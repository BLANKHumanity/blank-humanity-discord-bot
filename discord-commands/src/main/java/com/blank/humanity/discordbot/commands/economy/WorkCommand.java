package com.blank.humanity.discordbot.commands.economy;

import com.blank.humanity.discordbot.aop.DiscordCommand;
import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.entities.user.ClaimDataType;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

@DiscordCommand("work")
public class WorkCommand extends AbstractCommand {

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        FormattingData formattingData = getBlankUserService()
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
