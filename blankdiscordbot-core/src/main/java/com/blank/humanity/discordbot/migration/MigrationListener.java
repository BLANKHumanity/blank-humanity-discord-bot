package com.blank.humanity.discordbot.migration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.BlankUserService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class MigrationListener extends ListenerAdapter {

    @Autowired
    private JDA jda;

    @Autowired
    private BlankUserService blankUserService;

    private final Pattern coinPattern = Pattern
	    .compile("^(.{3,32})#([0-9]{4}) has ([0-9]{1,10}) [:a-zA-Z]+$");

    @PostConstruct
    void setupCommand() {
	jda.addEventListener(this);
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent mre) {
	if (mre.isFromGuild() && mre.getAuthor().isBot()) {
	    mre.getMessage().getEmbeds().forEach(embed -> {
		String description = embed.getDescription();
		if (description != null) {
		    Matcher matcher = coinPattern.matcher(description);
		    if (matcher.matches()) {
			checkEmbed(mre.getTextChannel(), matcher);
		    }
		}
	    });
	}
    }

    private void checkEmbed(TextChannel textChannel, Matcher matcher) {
	BlankUser user = blankUserService
		.getUser(textChannel.getGuild().getIdLong(), matcher.group(1),
			matcher.group(2));
	if (user == null) {
	    textChannel
		    .sendMessage(matcher.group(1) + "#" + matcher.group(2)
			    + " please write a Message in the normal chat first before trying to migrate :D")
		    .queue();
	    return;
	}

	int mee6Balance = Integer.valueOf(matcher.group(3));
	if (!user.isMigrated()) {
	    blankUserService
		    .migrateUser(user.getDiscordId(), user.getGuildId(),
			    mee6Balance);
	    textChannel
		    .sendMessage("<@" + user.getDiscordId()
			    + "> migrated successfully with a balance of "
			    + mee6Balance)
		    .queue();
	}
    }

}
