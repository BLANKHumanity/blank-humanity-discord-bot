package com.blank.humanity.discordbot.wallet.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractHiddenCommand;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.config.WalletVerifyConfig;
import com.blank.humanity.discordbot.wallet.messages.WalletFormatDataKey;
import com.blank.humanity.discordbot.wallet.messages.WalletMessageType;
import com.blank.humanity.discordbot.wallet.service.DiscordWalletService;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class VerifyCommand extends AbstractHiddenCommand {

    @Autowired
    private DiscordWalletService discordWalletService;

    @Autowired
    private WalletVerifyConfig walletVerifyConfig;

    @Override
    public String getCommandName() {
        return "verify";
    }

    @Override
    public CommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        return commandData;
    }

    @Override
    protected void onCommand(@NonNull GenericCommandInteractionEvent event) {
        BlankUser user = getUser();

        String salt = discordWalletService.createVerifyWalletSalt(user);

        reply(getBlankUserService()
            .createFormattingData(user,
                WalletMessageType.WALLET_VERIFY_DISPLAY_LINK)
            .dataPairing(WalletFormatDataKey.WALLET_VERIFY_LINK,
                walletVerifyConfig.getBaseUrl() + "?salt=" + salt
                    + "&discord_id=" + user.getDiscordId())
            .build());
    }

}
