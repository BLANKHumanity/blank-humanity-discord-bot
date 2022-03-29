package com.blank.humanity.discordbot.item.actions;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

@Component
public class RandomSelectionAction implements ExecutableItemAction {

    @Autowired
    private BlankUserService blankUserService;

    @Autowired
    private SecureRandom random;

    @Override
    public ItemActionStatus executeAction(BlankUser user,
        ItemActionState itemActionState) {

        List<String> selections = itemActionState.keys("selections");

        if (selections.isEmpty()) {
            itemActionState
                .reply(error(blankUserService, user,
                    itemActionState.getItemDefinition().getId(), "selections"));
            return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
        }

        String name = itemActionState.getStringProperty("name");

        if (name == null || name.isBlank()) {
            itemActionState
                .reply(error(blankUserService, user,
                    itemActionState.getItemDefinition().getId(), "name"));
            return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
        }

        List<Long> numbers = selections
            .stream()
            .map(key -> itemActionState
                .getProperty(key + ".probability", Long::parseLong))
            .toList();

        long sum = numbers
            .stream()
            .mapToLong(Long::longValue)
            .sum();

        long randomNumber = random.nextLong(0, sum);

        int index = -1;
        while (randomNumber >= 0) {
            index++;
            randomNumber -= numbers.get(index);
        }

        itemActionState
            .setEnvironment(name, itemActionState
                .getStringProperty(selections.get(index) + ".value"));

        return itemActionState.doNext(user);
    }

}
