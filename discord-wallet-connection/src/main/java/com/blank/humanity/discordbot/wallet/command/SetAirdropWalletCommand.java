package com.blank.humanity.discordbot.wallet.command;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Address;

import com.blank.humanity.discordbot.commands.AbstractHiddenCommand;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.wallet.messages.WalletFormatDataKey;
import com.blank.humanity.discordbot.wallet.messages.WalletMessageType;
import com.blank.humanity.discordbot.wallet.service.AirdropWalletService;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class SetAirdropWalletCommand extends AbstractHiddenCommand {

    private static final Pattern ethAddressRegex = Pattern
        .compile("0x[a-fA-F0-9]{40}");

    private static final String WALLET = "wallet";

    @Autowired
    private AirdropWalletService airdropWalletService;

    @Override
    public String getCommandName() {
        return "set-airdrop-wallet";
    }

    @Override
    public CommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        commandData
            .addOption(OptionType.STRING, WALLET,
                definition.getOptionDescription(WALLET), true);
        return commandData;
    }

    @Override
    protected void onCommand(@NonNull GenericCommandInteractionEvent event) {
        Optional<Address> address = event.getOption(WALLET, this::toAddress);

        if (address.isEmpty()) {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    WalletMessageType.SET_AIRDROP_WALLET_WRONG_FORMAT_ERROR)
                .build());
            return;
        }

        Address wallet = address.get();

        airdropWalletService.setAirdropWallet(getUser(), wallet);

        reply(getBlankUserService()
            .createFormattingData(getUser(),
                WalletMessageType.SET_AIRDROP_WALLET_SUCCESS)
            .dataPairing(WalletFormatDataKey.AIRDROP_WALLET,
                wallet.toString())
            .build());
    }

    private Optional<Address> toAddress(OptionMapping mapping) {
        return Optional
            .ofNullable(mapping)
            .map(OptionMapping::getAsString)
            .filter(address -> ethAddressRegex.matcher(address).matches())
            .map(Address::new);
    }

}
