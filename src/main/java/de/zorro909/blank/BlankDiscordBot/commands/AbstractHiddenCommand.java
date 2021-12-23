package de.zorro909.blank.BlankDiscordBot.commands;

public abstract class AbstractHiddenCommand extends AbstractCommand {

    @Override
    protected boolean isEphemeral() {
	return true;
    }

}
