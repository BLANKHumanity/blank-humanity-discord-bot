package com.blank.humanity.discordbot.wallet.command.emotes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.config.InitializerEmoteConfig;
import com.blank.humanity.discordbot.wallet.entities.EmoteDefinition;
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
            .keySet()
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
        EmoteDefinition emoteDefinition = initializerEmoteConfig
            .getInitializers()
            .get(emote);

        if (!isOwner(getUser(), initializerId)) {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    WalletMessageType.INITIALIZER_EMOTE_NFT_NOT_OWNED)
                .dataPairing(WalletFormatDataKey.NFT_ID, initializerId)
                .build());
            return;
        }

        byte[] image;
        try {
            image = drawEmoteImage(initializerId, emoteDefinition);
        } catch (IOException e) {
            log.error("Error occured during Emote Image Draw", e);
            sendErrorMessage(
                "The emote image unfortunately couldn't be finished. Please try again later.");
            return;
        }

        sendFile(initializerId + "_" + emote.replace(" ", "_") + ".png", image);

        MessageEmbed embed = new EmbedBuilder()
            .setImage("attachment://" + initializerId + "_" + emote + ".png")
            .build();
        reply(embed);
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

    private byte[] drawEmoteImage(long initializerId,
        EmoteDefinition emoteDefinition) throws IOException {
        byte[] initializerImageData = nftResolverService
            .fetchNftImage(INITIALIZER_ADDRESS, initializerId)
            .orElseThrow();
        BufferedImage initializerImage = ImageIO
            .read(new ByteArrayInputStream(initializerImageData));
        BufferedImage emote = ImageIO
            .read(emoteDefinition.getEmoteImage());

        BufferedImage emoteImage = new BufferedImage(540, 450,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = emoteImage.createGraphics();

        RenderingHints renderingHints = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RenderingHints rh = new RenderingHints(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        renderingHints.add(rh);
        rh = new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION,
            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        renderingHints.add(rh);

        graphics.setRenderingHints(renderingHints);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, emoteImage.getWidth(), emoteImage.getHeight());
        graphics.setColor(Color.BLACK);
        graphics.drawImage(initializerImage, 20, 20, 370, 370, null);

        graphics.drawImage(emote, 390, 20, null);

        String caption = emoteDefinition
            .getCaption()
            .replace("%(number)", String.valueOf(initializerId));

        graphics.setFont(new Font("Fira Code", Font.BOLD, 20));
        graphics.drawString(caption, 120, 430);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(emoteImage, "png", baos);
        return baos.toByteArray();
    }

}
