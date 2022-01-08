package de.zorro909.blank.BlankDiscordBot.commands;

public abstract class AbstractHiddenCommand extends AbstractCommand {

    public AbstractHiddenCommand(String command) {
	super(command);
    }

    @Override
    protected boolean isEphemeral() {
	return true;
    }

}
