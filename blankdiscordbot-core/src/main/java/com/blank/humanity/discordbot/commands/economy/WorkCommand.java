package com.blank.humanity.discordbot.commands.economy;

import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.user.ClaimDataType;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class WorkCommand extends AbstractCommand {

    @Override
    public String getCommandName() {
        return "work";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData workCommand,
        CommandDefinition definition) {
        return workCommand;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        FormattingData formattingData = blankUserService
            .claimReward(getUser(), ClaimDataType.WORK_CLAIM)
            .build();

        if (formattingData.success()) {
            reply(formattingData
                .messageType(EconomyMessageType.WORK_COMMAND_MESSAGE));
        } else {
            reply(formattingData
                .messageType(
                    EconomyMessageType.WORK_COMMAND_ALREADY_CLAIMED_MESSAGE));
        }
    }
}
