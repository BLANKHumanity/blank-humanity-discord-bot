package com.blank.humanity.discordbot.commands;

public abstract class AbstractHiddenCommand extends AbstractCommand {

    @Override
    protected boolean isEphemeral() {
	return true;
    }

}
