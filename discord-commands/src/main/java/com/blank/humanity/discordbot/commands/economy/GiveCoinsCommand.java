package com.blank.humanity.discordbot.commands.economy;

import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class GiveCoinsCommand extends AbstractCommand {

    private static final String USER = "user";

    private static final String AMOUNT = "amount";

    @Override
    public String getCommandName() {
        return "give-coins";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        commandData
            .addOption(OptionType.USER, USER,
                definition.getOptionDescription(USER),
                true);
        commandData
            .addOption(OptionType.INTEGER, AMOUNT,
                definition.getOptionDescription(AMOUNT),
                true);
        return commandData;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        Member receiver = event.getOption(USER).getAsMember();
        BlankUser receivingUser = getBlankUserService().getUser(receiver);

        int amount = (int) event.getOption(AMOUNT).getAsLong();

        getBlankUserService().increaseUserBalance(receivingUser, amount);
        reply(getBlankUserService()
            .createFormattingData(receivingUser,
                EconomyMessageType.GIVE_COINS_COMMAND)
            .dataPairing(EconomyFormatDataKey.REWARD_AMOUNT, amount)
            .build());
    }

}
