package com.blank.humanity.discordbot.wallet.command.emotes;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.config.InitializerEmoteConfig;
import com.blank.humanity.discordbot.wallet.messages.WalletFormatDataKey;
import com.blank.humanity.discordbot.wallet.messages.WalletMessageType;
import com.blank.humanity.discordbot.wallet.service.DiscordWalletService;
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
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Slf4j
@Component
public class InitializerEmoteCommand extends AbstractCommand {

    private static final String EMOTE = "emote";
    private static final String INITIALIZER = "initializer";
    private static final String INITIALIZER_ADDRESS = "0x881d9c2f229323aad28a9c9045111e30e1f1eb25";
    private static final String EMOTE_URL = "https://app.blankhumanity.com/api/emote/%d/%s/%s";

    @Setter(onMethod = @__({ @Autowired }))
    private InitializerEmoteConfig initializerEmoteConfig;

    @Setter(onMethod = @__({ @Autowired }))
    private DiscordWalletService discordWalletService;

    @Setter(onMethod = @__({ @Autowired }))
    private NftResolverService nftResolverService;

    @Override
    public String getCommandName() {
        return EMOTE;
    }

    @Override
    public CommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        OptionData initializer = new OptionData(OptionType.INTEGER,
            INITIALIZER, definition.getOptionDescription(INITIALIZER),
            true, true);
        initializer.setMinValue(0);
        initializer.setMaxValue(968);

        OptionData emote = new OptionData(OptionType.STRING, EMOTE,
            definition.getOptionDescription(EMOTE), true);
        List<Command.Choice> emoteChoices = initializerEmoteConfig
            .getInitializers()
            .stream()
            .map(key -> new Command.Choice(key, key))
            .toList();
        emote.addChoices(emoteChoices);

        commandData.addOptions(initializer, emote);
        return commandData;
    }

    @Override
    protected void onCommand(@NonNull GenericCommandInteractionEvent event) {
        long initializerId = event
            .getOption(INITIALIZER, OptionMapping::getAsLong);
        String emote = event.getOption(EMOTE, OptionMapping::getAsString);

        if (!isOwner(getUser(), initializerId)) {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    WalletMessageType.INITIALIZER_EMOTE_NFT_NOT_OWNED)
                .dataPairing(WalletFormatDataKey.NFT_ID, initializerId)
                .build());
            return;
        }

        ResponseEntity<byte[]> image = fetchEmoteImage(initializerId, emote);

        if (image.getStatusCode().is5xxServerError()) {
            log
                .error("Error occured during Emote Image Fetching",
                    image.toString());
            sendErrorMessage(
                "The emote image unfortunately couldn't be finished. Please try again later.");
            return;
        } else if (image.getStatusCode().is4xxClientError()) {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    WalletMessageType.INITIALIZER_EMOTE_NOT_BOUGHT)
                .dataPairing(WalletFormatDataKey.EMOTE_ID, emote)
                .build());
            return;
        }

        sendFile(initializerId + "_" + emote.replace(" ", "_") + ".png",
            image.getBody());

        MessageEmbed embed = new EmbedBuilder()
            .setImage("attachment://" + initializerId + "_" + emote + ".png")
            .build();
        reply(embed);
    }

    private ResponseEntity<byte[]> fetchEmoteImage(long initializerId,
        String emoteId) {
        RestTemplate rest = new RestTemplate();
        try {
            return rest
                .exchange(
                    EMOTE_URL
                        .formatted(initializerId, emoteId,
                            initializerEmoteConfig.getSize()),
                    HttpMethod.GET, null, byte[].class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (RestClientException e) {
            log
                .warn("An error occured during Emote Image fetch ("
                    + e.getMessage() + ")", e);
            return ResponseEntity
                .internalServerError()
                .build();
        }
    }

    @Override
    protected Collection<Command.Choice> onAutoComplete(
        @NonNull CommandAutoCompleteInteractionEvent event) {
        return nftResolverService
            .findOwnedNFTs(INITIALIZER_ADDRESS, getUser())
            .stream()
            .map(tokenId -> new Command.Choice("Initializer #" + tokenId,
                tokenId))
            .toList();
    }

    private boolean isOwner(BlankUser user, long nftId) {
        return nftResolverService
            .findBlankUserOwner(INITIALIZER_ADDRESS, nftId)
            .filter(owner -> owner.getId().equals(user.getId()))
            .isPresent();
    }

}
