package com.blank.humanity.discordbot.wallet.service;

import java.util.Optional;

public interface InitializerEmoteService {

    public boolean isEmoteUnlocked(int initializer, String emote);

    public boolean unlockEmote(int initializer, String emote);

    public Optional<byte[]> fetchEmoteImage(int initializer, String emote);

}
