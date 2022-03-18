package com.blank.humanity.discordbot.commands;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractHiddenCommand extends AbstractCommand {

    @Override
    public boolean isEphemeral() {
        return true;
    }

}
