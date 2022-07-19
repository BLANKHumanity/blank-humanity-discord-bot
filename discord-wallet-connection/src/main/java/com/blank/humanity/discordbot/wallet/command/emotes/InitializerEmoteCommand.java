package com.blank.humanity.discordbot.wallet.command.emotes;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import com.blank.humanity.discordbot.aop.Argument;
import com.blank.humanity.discordbot.aop.DiscordCommand;
import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.config.InitializerEmoteConfig;
import com.blank.humanity.discordbot.wallet.messages.EmoteFormatDataKey;
import com.blank.humanity.discordbot.wallet.messages.EmoteMessageType;
import com.blank.humanity.discordbot.wallet.service.DiscordWalletService;
import com.blank.humanity.discordbot.wallet.service.InitializerEmoteService;
import com.blank.humanity.discordbot.wallet.service.NftResolverService;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@DiscordCommand(name = "emote")
@Argument(name = "initializer", type = OptionType.INTEGER, autocomplete = true, minValue = 0, maxValue = 968)
@Argument(name = "emote", autocomplete = true)
public class InitializerEmoteCommand extends AbstractCommand {

    private static final String EMOTE = "emote";
    private static final String INITIALIZER = "initializer";
    public static final String INITIALIZER_ADDRESS = "0x881d9c2f229323aad28a9c9045111e30e1f1eb25";

    @Setter(onMethod = @__({ @Autowired }))
    private InitializerEmoteConfig initializerEmoteConfig;

    @Setter(onMethod = @__({ @Autowired }))
    private DiscordWalletService discordWalletService;

    @Setter(onMethod = @__({ @Autowired }))
    private NftResolverService nftResolverService;

    @Setter(onMethod = @__({ @Autowired }))
    private InitializerEmoteService initializerEmoteService;

    @Override
    protected void onCommand(@NonNull GenericCommandInteractionEvent event) {
        int initializerId = event
            .getOption(INITIALIZER, OptionMapping::getAsInt);
        String emote = event.getOption(EMOTE, OptionMapping::getAsString);

        if (!isOwner(getUser(), initializerId)) {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    EmoteMessageType.INITIALIZER_EMOTE_NFT_NOT_OWNED)
                .dataPairing(EmoteFormatDataKey.NFT_ID, initializerId)
                .build());
            return;
        }

        if (!initializerEmoteService.isEmoteUnlocked(initializerId, emote)) {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    EmoteMessageType.INITIALIZER_EMOTE_NOT_BOUGHT)
                .dataPairing(EmoteFormatDataKey.EMOTE_ID, emote)
                .build());
            return;
        }

        Optional<byte[]> image = initializerEmoteService
            .fetchEmoteImage(initializerId, emote);

        if (image.isEmpty()) {
            sendErrorMessage(
                "The emote image unfortunately couldn't be finished. Please try again later.");
            return;
        }

        sendFile(initializerId + "_" + emote.replace(" ", "_") + ".png",
            image.get());

        MessageEmbed embed = new EmbedBuilder()
            .setImage("attachment://" + initializerId + "_" + emote + ".png")
            .build();
        reply(embed);
    }

    @Override
    protected Collection<Command.Choice> onAutoComplete(
        @NonNull CommandAutoCompleteInteractionEvent event) {
        if (event.getFocusedOption().getName().equals(INITIALIZER)) {
            return nftResolverService
                .findOwnedNFTs(INITIALIZER_ADDRESS, getUser())
                .stream()
                .map(tokenId -> new Command.Choice("Initializer #" + tokenId,
                    tokenId))
                .filter(choice -> choice
                    .getName()
                    .toLowerCase()
                    .contains(
                        event.getFocusedOption().getValue().toLowerCase()))
                .limit(25)
                .toList();
        } else {
            Integer initializer = event
                .getOption(INITIALIZER, OptionMapping::getAsInt);

            return Stream
                .concat(initializerEmoteConfig.getUnlocked().stream(),
                    initializerEmoteConfig
                        .getLocked()
                        .stream()
                        .filter(emote -> initializer == null
                            || initializerEmoteService
                                .isEmoteUnlocked(initializer, emote)))
                .map(emote -> new Command.Choice(emote, emote))
                .toList();
        }
    }

    private boolean isOwner(BlankUser user, long nftId) {
        return nftResolverService
            .findBlankUserOwner(INITIALIZER_ADDRESS, nftId)
            .filter(owner -> owner.getId().equals(user.getId()))
            .isPresent();
    }

}
