package com.blank.humanity.discordbot.wallet.command.emotes;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.blank.humanity.discordbot.aop.Argument;
import com.blank.humanity.discordbot.aop.DiscordCommand;
import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.entities.item.Item;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.service.EventService;
import com.blank.humanity.discordbot.services.InventoryService;
import com.blank.humanity.discordbot.utils.Wrapper;
import com.blank.humanity.discordbot.wallet.config.InitializerEmoteConfig;
import com.blank.humanity.discordbot.wallet.messages.EmoteFormatDataKey;
import com.blank.humanity.discordbot.wallet.messages.EmoteMessageType;
import com.blank.humanity.discordbot.wallet.service.InitializerEmoteService;
import com.blank.humanity.discordbot.wallet.service.NftResolverService;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Slf4j
@DiscordCommand("learn-emote")
@Argument(name = "initializer", type = OptionType.INTEGER, autocomplete = true, minValue = 0, maxValue = 968)
@Argument(name = "emote", autocomplete = true)
public class LearnEmoteCommand extends AbstractCommand {

    private static final String INITIALIZER = "initializer";

    @Setter(onMethod = @__({ @Autowired }))
    private NftResolverService nftResolverService;

    @Setter(onMethod = @__({ @Autowired }))
    private InitializerEmoteService initializerEmoteService;

    @Setter(onMethod = @__({ @Autowired }))
    private InventoryService inventoryService;

    @Setter(onMethod = @__({ @Autowired }))
    private InitializerEmoteConfig initializerEmoteConfig;

    @Setter(onMethod = @__({ @Autowired }))
    private EventService eventService;

    @Override
    protected void onCommand(@NonNull GenericCommandInteractionEvent event) {
        int initializer = event
            .getOption(INITIALIZER, OptionMapping::getAsInt);

        String emote = event.getOption("emote", OptionMapping::getAsString);

        if (!initializerEmoteConfig.getLocked().contains(emote)) {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    EmoteMessageType.INITIALIZER_LEARN_UNKNOWN_EMOTE)
                .dataPairing(EmoteFormatDataKey.EMOTE_ID, emote)
                .build());
            return;
        }

        if (initializerEmoteService.isEmoteUnlocked(initializer, emote)) {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    EmoteMessageType.INITIALIZER_EMOTE_ALREADY_LEARNT)
                .dataPairing(EmoteFormatDataKey.EMOTE_ID, emote)
                .dataPairing(EmoteFormatDataKey.NFT_ID, initializer)
                .build());
            return;
        }

        Optional<Item> learnItem = getLearnItem(getUser(), emote);

        if (learnItem.isEmpty()
            || !inventoryService
                .removeItem(getUser(), learnItem.get().getItemId(), 1)) {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    EmoteMessageType.INITIALIZER_EMOTE_LEARN_NO_ITEM)
                .dataPairing(EmoteFormatDataKey.EMOTE_ID, emote)
                .dataPairing(EmoteFormatDataKey.NFT_ID, initializer)
                .build());
            return;
        }

        reply(getBlankUserService()
            .createFormattingData(getUser(),
                EmoteMessageType.INITIALIZER_LEARNS_EMOTE)
            .dataPairing(EmoteFormatDataKey.EMOTE_ID, emote)
            .dataPairing(EmoteFormatDataKey.NFT_ID, initializer)
            .build());

        initializerEmoteService.unlockEmote(initializer, emote);
    }

    @NonNull
    @Override
    protected Collection<Command.Choice> onAutoComplete(
        @NonNull CommandAutoCompleteInteractionEvent autoCompleteEvent) {
        if (autoCompleteEvent
            .getFocusedOption()
            .getName()
            .equals(INITIALIZER)) {
            return nftResolverService
                .findOwnedNFTs(InitializerEmoteCommand.INITIALIZER_ADDRESS,
                    getUser())
                .stream()
                .map(initializerId -> new Command.Choice(
                    "Initializer #" + initializerId, initializerId))
                .filter(choice -> containsIgnoreCase(choice.getName(),
                    autoCompleteEvent
                        .getFocusedOption()
                        .getValue()))
                .limit(25)
                .toList();
        } else {
            int initializer = autoCompleteEvent
                .getOption(INITIALIZER, -1, OptionMapping::getAsInt);

            return initializerEmoteConfig
                .getLocked()
                .stream()
                .filter(emote -> containsIgnoreCase(emote,
                    autoCompleteEvent
                        .getFocusedOption()
                        .getValue()))
                .filter(emote -> initializer == -1 || !initializerEmoteService
                    .isEmoteUnlocked(initializer, emote))
                .filter(emote -> getLearnItem(getUser(), emote)
                    .map(item -> item.getAmount() > 0)
                    .orElse(false))
                .map(emote -> new Command.Choice(emote, emote))
                .limit(25)
                .toList();
        }
    }

    private boolean containsIgnoreCase(String string, String search) {
        return string
            .toLowerCase()
            .contains(search.toLowerCase());
    }

    private Optional<Item> getLearnItem(BlankUser user, String emote) {
        return inventoryService
            .getItemDefinition("learnTo" + emote)
            .map(ItemDefinition::getId)
            .flatMap(Wrapper.wrap(inventoryService::getItem, user));
    }
}
