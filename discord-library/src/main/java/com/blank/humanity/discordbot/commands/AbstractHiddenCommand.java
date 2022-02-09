package com.blank.humanity.discordbot.commands;

public abstract class AbstractHiddenCommand extends AbstractCommand {

    protected AbstractHiddenCommand(String command) {
	super(command);
    }

    @Override
    protected boolean isEphemeral() {
	return true;
    }

}
