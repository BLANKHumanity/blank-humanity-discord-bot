package com.blank.humanity.discordbot.commands.games;

import java.util.Optional;

import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;
import com.blank.humanity.discordbot.utils.menu.DiscordMenuActionWrapper;
import com.blank.humanity.discordbot.utils.menu.DiscordMenuInteraction;
import com.blank.humanity.discordbot.utils.menu.WrapperChain;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

@Slf4j
public class GameInteractionEventExecutor<E extends GenericComponentInteractionCreateEvent, M extends DiscordMenu>
    implements DiscordMenuActionWrapper<E, M> {

    private AbstractGame game;

    public GameInteractionEventExecutor(AbstractGame game) {
        this.game = game;
    }

    @Override
    public boolean wrap(DiscordMenuInteraction<E, M> interaction,
        WrapperChain<E, M> chain) {
        E event = interaction.event();
        Member member = event.getMember();
        Message message = event.getMessage();

        BlankUser user = game
            .getBlankUserService()
            .getUser(member);

        Optional<GameMetadata> gameMetadata = game
            .getGameService()
            .getGameMetadata(user, game.getCommandName());

        if (gameMetadata.isEmpty()) {
            return false;
        }

        GameMetadata metadata = gameMetadata.get();

        DiscordMenu newMenu = null;

        try {
            game.setUser(user);
            game.setMember(member);

            log
                .info("Continuing Game via Menu: {} for {} (gameId: {})",
                    game.getCommandName(), user.getId(), metadata.getId());

            newMenu = game
                .onGameContinue(user, metadata, interaction.argument());
            if (game.getUnsentReply() != null) {
                message.editMessageEmbeds(game.getUnsentReply()).complete();
            }

            if (newMenu != null || metadata.isGameFinished()) {
                interaction.menu().discard();
                if (newMenu != null) {
                    newMenu
                        .buildMenu(game.getJda(), message,
                            game.getMenuService());
                }
            }
        } catch (Exception exc) {
            log.error("Error occured during Game Interaction", exc);

            FormattingData errorData = game
                .getBlankUserService()
                .createFormattingData(user, GenericMessageType.ERROR_MESSAGE)
                .dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
                    exc.getMessage())
                .build();
            String errorMessage = game
                .getMessageService()
                .format(errorData);
            MessageEmbed errorEmbed = new EmbedBuilder()
                .setDescription(errorMessage)
                .build();
            message.getTextChannel().sendMessageEmbeds(errorEmbed).complete();
        } finally {
            game.clearThreadLocals();
        }
        return true;
    }

}
