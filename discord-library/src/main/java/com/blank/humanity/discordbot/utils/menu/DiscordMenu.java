package com.blank.humanity.discordbot.utils.menu;

import com.blank.humanity.discordbot.services.MenuService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;

public interface DiscordMenu {
    
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
