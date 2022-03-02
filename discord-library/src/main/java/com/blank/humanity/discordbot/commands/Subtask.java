package com.blank.humanity.discordbot.commands;

import java.util.function.Consumer;

import com.blank.humanity.discordbot.utils.FormattingData;

public abstract interface Subtask extends Consumer<Consumer<FormattingData[]>> {

}
