package com.blank.humanity.discordbot.commands.economy;

import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class BalanceCommand extends AbstractCommand {

    @Override
    protected String getCommandName() {
        return "balance";
    }

    @Override
    protected SlashCommandData createCommandData(SlashCommandData commandData) {
        commandData
            .addOption(OptionType.USER, "user",
                getCommandDefinition().getOptionDescription("user"),
                false);
        return commandData;
    }

    @Override
    protected void onCommand(SlashCommandInteraction event) {
        OptionMapping option = event.getOption("user");
        Member member;
        if (option == null) {
            member = event.getMember();
        } else {
            member = option.getAsMember();
        }

        BlankUser blankUser = blankUserService
            .getUser(member.getIdLong(), member.getGuild().getIdLong());
        int balance = blankUser.getBalance();

        reply(event,
            blankUserService
                .createFormattingData(blankUser,
                    EconomyMessageType.BALANCE_COMMAND_MESSAGE)
                .dataPairing(EconomyFormatDataKey.BALANCE, balance)
                .build());
    }

}
