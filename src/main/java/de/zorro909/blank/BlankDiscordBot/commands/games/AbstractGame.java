package de.zorro909.blank.BlankDiscordBot.commands.games;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.database.GameMetadataDao;
import de.zorro909.blank.BlankDiscordBot.entities.game.GameMetadata;
import de.zorro909.blank.BlankDiscordBot.entities.game.GameType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import de.zorro909.blank.BlankDiscordBot.utils.menu.ReactionMenu;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

@Getter
public abstract class AbstractGame extends AbstractCommand {

    @Autowired
    private GameMetadataDao gameMetadataDao;

    private GameType gameType;

    private MessageEmbed cachedEdit;

    public AbstractGame(GameType gameType) {
	super(gameType.getCommandName());
	this.gameType = gameType;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	BlankUser user = getBlankUserService().getUser(event);

	Optional<GameMetadata> gameMetadata = gameMetadataDao
		.findByUserAndGame(user, getGameType());

	GameMetadata metadata = new GameMetadata();

	if (gameMetadata.isPresent()) {
	    metadata = gameMetadata.get();

	    LocalDateTime possibleEarlierTime = LocalDateTime
		    .now()
		    .minus(getGameType().getCooldownAmount(),
			    getGameType().getCooldownTimeUnit());

	    if (metadata.getLastPlayed().isAfter(possibleEarlierTime)) {
		long minutes = ChronoUnit.MINUTES
			.between(possibleEarlierTime, metadata.getLastPlayed());
		long seconds = ChronoUnit.SECONDS
			.between(possibleEarlierTime, metadata.getLastPlayed())
			% 60;

		reply(event, getBlankUserService()
			.createFormattingData(user,
				MessageType.GAME_ON_COOLDOWN)
			.dataPairing(FormatDataKey.COOLDOWN_MINUTES, minutes)
			.dataPairing(FormatDataKey.COOLDOWN_SECONDS, seconds)
			.dataPairing(FormatDataKey.GAME_NAME,
				getGameType().getDisplayName())
			.build());
		return;
	    }
	} else {
	    metadata.setGame(getGameType());
	    metadata.setGameFinished(true);
	    metadata.setLastPlayed(LocalDateTime.of(2000, 1, 1, 0, 0));
	    metadata.setUser(user);
	    if (getGameType().hasMetadataClass()) {
		metadata
			.setMetadataClassname(
				getGameType().getMetadataClass().getName());
	    }
	    metadata = gameMetadataDao.save(metadata);
	}

	ReactionMenu menu = null;
	if (metadata.isGameFinished()) {
	    // Previous Game was finished
	    metadata.setGameFinished(false);
	    menu = onGameStart(event, user, metadata);
	} else {
	    synchronized (this) {
		cachedEdit = null;
		menu = onGameContinue(user, metadata, event.getOptions(),
			(formattingData) -> {
			    EmbedBuilder builder = new EmbedBuilder();
			    builder.setDescription(format(formattingData));

			    cachedEdit = builder.build();
			});
		reply(event, cachedEdit);
	    }
	}
	if (menu != null) {
	    addReactionMenu(event, menu);
	}
    }

    private boolean reactionInteractionWrapper(MessageReactionAddEvent event,
	    ReactionMenu menu, Object argument) {	
	BlankUser user = getBlankUserService()
		.getUser(event.retrieveMember().complete());

	Optional<GameMetadata> gameMetadata = gameMetadataDao
		.findByUserAndGame(user, getGameType());

	if (gameMetadata.isEmpty()) {
	    return false;
	}

	ReactionMenu newMenu = null;

	synchronized (this) {
	    cachedEdit = null;
	    newMenu = onGameContinue(user, gameMetadata.get(), argument,
		    (formattingData) -> {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setDescription(format(formattingData));

			cachedEdit = builder.build();
		    });
	    if (cachedEdit != null) {
		event
			.getChannel()
			.editMessageEmbedsById(event.getMessageIdLong(),
				cachedEdit)
			.complete();
	    }
	}
	if (newMenu != null) {
	    menu.discard();
	    Message message = event
		    .getChannel()
		    .retrieveMessageById(event.getMessageIdLong())
		    .complete();
	    message.clearReactions().complete();
	    newMenu
		    .buildMenu(getJda(),
			    event
				    .getChannel()
				    .retrieveMessageById(
					    event.getMessageIdLong())
				    .complete(),
			    getTaskScheduler(), getTransactionExecutor());
	}
	return true;
    }

    protected void createMenuEntry(ReactionMenu menu, String emoji,
	    Object argument) {
	menu
		.addMenuAction(emoji,
			(event) -> reactionInteractionWrapper(event, menu,
				argument));
    }

    protected abstract ReactionMenu onGameStart(SlashCommandEvent event,
	    BlankUser user, GameMetadata metadata);

    protected abstract ReactionMenu onGameContinue(BlankUser user,
	    GameMetadata metadata, Object argument,
	    Consumer<FormattingData> messageEdit);

    protected void abort(GameMetadata metadata) {
	metadata.clearMetadata();
	metadata.setGameFinished(true);
    }

    protected void finish(GameMetadata metadata) {
	metadata.clearMetadata();
	metadata.setGameFinished(true);
	metadata.setLastPlayed(LocalDateTime.now());
    }

    protected void finish(long gameId) {
	Optional<GameMetadata> metadata = gameMetadataDao.findById(gameId);
	if (metadata.isEmpty()) {
	    throw new RuntimeException(
		    "No Game with ID '" + gameId + "' found!");
	}
	finish(metadata.get());
    }

}
