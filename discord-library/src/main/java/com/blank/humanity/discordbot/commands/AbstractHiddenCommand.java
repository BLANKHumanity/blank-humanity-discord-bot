package com.blank.humanity.discordbot.commands;

public abstract class AbstractHiddenCommand extends AbstractCommand {

    @Override
    public boolean isEphemeral() {
        return true;
    }

}
