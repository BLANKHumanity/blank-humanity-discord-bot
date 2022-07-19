package com.blank.humanity.discordbot.commands.economy;

import com.blank.humanity.discordbot.aop.Argument;
import com.blank.humanity.discordbot.aop.DiscordCommand;
import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@DiscordCommand(name = "balance")
@Argument(name = "user", type = OptionType.USER, required = false)
public class BalanceCommand extends AbstractCommand {

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        Member member = event
            .getOption("user", this::getMember, OptionMapping::getAsMember);

        BlankUser blankUser = getBlankUserService()
            .getUser(member.getIdLong(), member.getGuild().getIdLong());

        reply(getBlankUserService()
            .createFormattingData(blankUser,
                EconomyMessageType.BALANCE_COMMAND_MESSAGE)
            .build());
    }

}
