package com.blank.humanity.discordbot.commands.items;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.items.ItemActionDefinition;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.item.actions.ItemAction;
import com.blank.humanity.discordbot.item.actions.ItemActionImpl;
import com.blank.humanity.discordbot.item.actions.ItemActionState;
import com.blank.humanity.discordbot.item.actions.ItemActionStatus;
import com.blank.humanity.discordbot.services.InventoryService;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.RestAction;

@Slf4j
@Component
public class ItemUseCommand extends AbstractCommand {

    private static final String ITEM = "item";
    private static final String AMOUNT = "amount";

    @Setter(onMethod = @__({ @Autowired }))
    private InventoryService inventoryService;

    @Setter(onMethod = @__({ @Autowired }))
    private ApplicationContext applicationContext;

    @Override
    public String getCommandName() {
        return "use";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        commandData
            .addOption(OptionType.STRING, ITEM,
                definition.getOptionDescription(ITEM),
                true, true);
        OptionData amount = new OptionData(OptionType.INTEGER, AMOUNT,
            definition.getOptionDescription(AMOUNT));
        amount.setMinValue(1);
        commandData.addOptions(amount);
        return commandData;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        String item = event.getOption(ITEM, OptionMapping::getAsString);

        int amount = event
            .getOption(AMOUNT, 1l, OptionMapping::getAsLong)
            .intValue();

        Optional<ItemDefinition> itemDefinition = inventoryService
            .getItemDefinition(item);

        ExecutableItemAction[] actions = itemDefinition
            .stream()
            .map(ItemDefinition::getActions)
            .flatMap(Arrays::stream)
            .map(ItemActionDefinition::getAction)
            .map(ItemActionImpl::valueOf)
            .map(ItemAction::getExecutableItemAction)
            .map(applicationContext::getBean)
            .toArray(size -> new ExecutableItemAction[size]);

        if (itemDefinition.isEmpty()) {
            FormattingData data = getBlankUserService()
                .createFormattingData(getUser(),
                    ItemMessageType.ITEM_NOT_EXISTS)
                .dataPairing(ItemFormatDataKey.ITEM_NAME, item)
                .build();
            reply(data);
            return;
        }

        ItemDefinition resolvedItemDefinition = itemDefinition.get();
        int itemId = resolvedItemDefinition.getId();

        if (actions.length == 0) {
            FormattingData data = getBlankUserService()
                .createFormattingData(getUser(),
                    ItemMessageType.ITEM_USE_ACTION_UNDEFINED)
                .dataPairing(ItemFormatDataKey.ITEM_ID, itemId)
                .dataPairing(ItemFormatDataKey.ITEM_NAME, item)
                .build();
            reply(data);
            return;
        }

        if (!inventoryService.removeItem(getUser(), itemId, amount)) {
            FormattingData data = getBlankUserService()
                .createFormattingData(getUser(),
                    ItemMessageType.ITEM_USE_NOT_OWNED)
                .dataPairing(ItemFormatDataKey.ITEM_ID, itemId)
                .dataPairing(ItemFormatDataKey.ITEM_NAME, item)
                .dataPairing(ItemFormatDataKey.ITEM_AMOUNT, amount)
                .build();
            reply(data);
            return;
        }

        ItemActionState itemActionState = new ItemActionState(
            event.getChannel().getIdLong(), resolvedItemDefinition, amount,
            getMessageService());

        ItemActionStatus status = ItemActionStatus.SUCCESS;

        try {
            for (int i = 0; i < actions.length
                && status == ItemActionStatus.SUCCESS; i++) {
                itemActionState.setActionIndex(i);
                status = actions[i]
                    .executeAction(getUser(), itemActionState);
            }
        } catch (RuntimeException exc) {
            log.error("Error occured during Item Usage", exc);
            throw exc;
        }
        if (status != ItemActionStatus.SUCCESS) {
            // On Error give Item back
            inventoryService.giveItem(getUser(), itemId, amount);
            return;
        }

        itemActionState
            .getEmbedsToSend()
            .entrySet()
            .stream()
            .map(entry -> getJda()
                .getTextChannelById(entry.getKey())
                .sendMessageEmbeds(entry.getValue())
                .map(i -> (Void) null))
            .reduce((action1,
                action2) -> action1 != null ? action1.and(action2) : action2)
            .ifPresent(RestAction::complete);
        MessageEmbed[] replyEmbeds = itemActionState
            .getEmbedsToReply()
            .toArray(size -> new MessageEmbed[size]);
        reply(replyEmbeds);

    }

    @Override
    protected Collection<Command.Choice> onAutoComplete(
        CommandAutoCompleteInteractionEvent event) {
        String itemName = event
            .getOption(ITEM, () -> "", OptionMapping::getAsString)
            .toLowerCase();

        return inventoryService
            .autoCompleteUserItems(getUser(), itemName);
    }

}
