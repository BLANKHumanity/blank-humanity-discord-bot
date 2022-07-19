package com.blank.humanity.discordbot.wallet.service.impl;

import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.blank.humanity.discordbot.service.EventService;
import com.blank.humanity.discordbot.utils.NamedFormatter;
import com.blank.humanity.discordbot.wallet.command.emotes.InitializerLearnsEmoteEvent;
import com.blank.humanity.discordbot.wallet.config.InitializerEmoteConfig;
import com.blank.humanity.discordbot.wallet.entities.InitializerEmoteUnlock;
import com.blank.humanity.discordbot.wallet.persistence.InitializerEmoteUnlockDao;
import com.blank.humanity.discordbot.wallet.service.InitializerEmoteService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InitializerEmoteServiceImpl implements InitializerEmoteService {

    @Setter(onMethod = @__({ @Autowired }))
    private InitializerEmoteUnlockDao initializerEmoteUnlockDao;

    @Setter(onMethod = @__({ @Autowired }))
    private EventService eventService;

    @Setter(onMethod = @__({ @Autowired }))
    private InitializerEmoteConfig initializerEmoteConfig;

    @Setter(onMethod = @__({ @Autowired }))
    private RestTemplate restTemplate;

    @Override
    public boolean isEmoteUnlocked(int initializer, String emote) {
        return initializerEmoteConfig.getUnlocked().contains(emote)
            || initializerEmoteUnlockDao
                .existsByInitializerAndEmote(initializer, emote);
    }

    @Override
    @Transactional
    public boolean unlockEmote(int initializer, String emote) {
        if (isEmoteUnlocked(initializer, emote)) {
            return false;
        }

        InitializerLearnsEmoteEvent learnEmoteEvent = new InitializerLearnsEmoteEvent(
            initializer, emote);
        try {
            eventService.publishEvent(learnEmoteEvent);
        } catch (JsonProcessingException e) {
            log.error("Error during Event publishing", e);
            return false;
        }
        return true;
    }

    @EventListener
    @Transactional
    public void receiveUnlockEmoteEvent(
        InitializerLearnsEmoteEvent learnsEmoteEvent) {
        InitializerEmoteUnlock unlock = new InitializerEmoteUnlock();
        unlock.setInitializer(learnsEmoteEvent.getInitializer());
        unlock.setEmote(learnsEmoteEvent.getEmote());
        initializerEmoteUnlockDao.save(unlock);
    }

    @Override
    public Optional<byte[]> fetchEmoteImage(int initializer, String emote) {
        try {
            String url = initializerEmoteConfig.getUrl();

            url = NamedFormatter
                .namedFormat(url,
                    Map
                        .of("initializer", initializer, "emote", emote,
                            "size", initializerEmoteConfig.getSize()));

            return Optional
                .of(restTemplate
                    .exchange(url, HttpMethod.GET, null, byte[].class)
                    .getBody());
        } catch (HttpStatusCodeException e) {
            return Optional.empty();
        } catch (RestClientException e) {
            log
                .warn("An error occured during Emote Image fetch ("
                    + e.getMessage() + ")", e);
            return Optional.empty();
        }
    }

}
