package com.blank.humanity.discordbot.exceptions.menu;

import lombok.experimental.StandardException;

@StandardException
public class NonUniqueInteractionId extends RuntimeException {

    public NonUniqueInteractionId(String duplicateId) {
        super("Interaction Id '" + duplicateId + "' already exists!");
    }

    private static final long serialVersionUID = -8823728070018744080L;

}
