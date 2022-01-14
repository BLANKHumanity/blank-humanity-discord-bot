package de.zorro909.blank.BlankDiscordBot.commands;

import java.util.function.Consumer;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;

public abstract interface Subtask extends Consumer<Consumer<FormattingData[]>> {

}
