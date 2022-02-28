package com.blank.humanity.discordbot.utils.menu;

import java.util.List;

import com.blank.humanity.discordbot.services.MenuService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

public interface DiscordMenu {
    
    public DiscordMenu allowedDiscordIds(List<Long> allowedIds);

    public DiscordMenu restricted(boolean restricted);

    public DiscordMenu singleUse(boolean singleUse);

    public DiscordMenu timeoutTask(Runnable timeoutTask);
    
    public Runnable timeoutTask();

    public void buildMenu(JDA jda, Message message, MenuService menuService);

    public void discard();

    public default void timeout() {
        discard();
        timeoutTask().run();
    }

}
