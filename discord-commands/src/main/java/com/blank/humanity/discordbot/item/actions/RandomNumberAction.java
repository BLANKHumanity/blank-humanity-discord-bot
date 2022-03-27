package com.blank.humanity.discordbot.item.actions;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

@Component
public class RandomNumberAction implements ExecutableItemAction {

    @Autowired
    private BlankUserService blankUserService;

    @Autowired
    private SecureRandom random;
    
    @Override
    public ItemActionStatus executeAction(BlankUser user,
        ItemActionState itemActionState) {

        String name = itemActionState.getStringProperty("name");
        Long min = itemActionState.getProperty("minimum", Long::parseLong, 0l);
        Long max = itemActionState.getProperty("maximum", Long::parseLong);

        if (name == null || min == null || max == null) {
            itemActionState
                .reply(error(blankUserService, user,
                    itemActionState.getItemDefinition().getId(),
                    "RandomNumberAction"));
            return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
        }

        long randomNumber = random.nextLong(min, max);
        
        itemActionState.setEnvironment(name, randomNumber);
        
        return ItemActionStatus.SUCCESS;
    }

}
