package com.blank.humanity.discordbot.commands.economy;

import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class BalanceCommand extends AbstractCommand {

    @Override
    public String getCommandName() {
        return "balance";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        commandData
            .addOption(OptionType.USER, "user",
                definition.getOptionDescription("user"), false);
        return commandData;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        Member member = event
            .getOption("user", this::getMember, OptionMapping::getAsMember);

        BlankUser blankUser = blankUserService
            .getUser(member.getIdLong(), member.getGuild().getIdLong());
        int balance = blankUser.getBalance();

        reply(blankUserService
            .createFormattingData(blankUser,
                EconomyMessageType.BALANCE_COMMAND_MESSAGE)
            .dataPairing(EconomyFormatDataKey.BALANCE, balance)
            .build());
    }

}
