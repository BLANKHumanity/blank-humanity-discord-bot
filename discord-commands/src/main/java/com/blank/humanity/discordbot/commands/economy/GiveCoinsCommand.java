package com.blank.humanity.discordbot.commands.economy;

import com.blank.humanity.discordbot.aop.Argument;
import com.blank.humanity.discordbot.aop.DiscordCommand;
import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@DiscordCommand(name = "give-coins")
@Argument(name = "user", type = OptionType.USER)
@Argument(name = "amount", type = OptionType.INTEGER)
public class GiveCoinsCommand extends AbstractCommand {

    private static final String USER = "user";

    private static final String AMOUNT = "amount";

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
