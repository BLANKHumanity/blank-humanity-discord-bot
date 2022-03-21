package com.blank.humanity.discordbot.services;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.transaction.annotation.Transactional;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.user.BlankUserMetadata;

import reactor.core.publisher.Flux;

public interface BlankUserMetadataService {

    @Transactional
    public BlankUserMetadata saveMetadata(BlankUser user, String key,
        String value);

    @Transactional
    public Optional<BlankUserMetadata> getMetadata(BlankUser user,
        String key);

    @Transactional
    public Stream<BlankUserMetadata> listUserMetadata(BlankUser user);

    @Transactional
    public Stream<BlankUserMetadata> listAllMetadataByKey(String key);

    public Flux<BlankUserMetadata> fluxListAllMetadataByKey(String key);

}
