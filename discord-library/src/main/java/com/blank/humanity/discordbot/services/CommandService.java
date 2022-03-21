package com.blank.humanity.discordbot.services;

import java.util.concurrent.CompletableFuture;

import com.blank.humanity.discordbot.commands.AbstractCommand;

import net.dv8tion.jda.api.interactions.Interaction;

public interface CommandService {

    public void registerCommand(AbstractCommand command);

    public CompletableFuture<?> updateCommand(String commandName);

    public void receiveInteraction(Interaction interaction);

}
