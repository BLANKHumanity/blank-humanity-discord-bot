package com.blank.humanity.discordbot.database;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.user.BlankUserMetadataImpl;

@Repository
public interface BlankUserMetadataDao
    extends JpaRepository<BlankUserMetadataImpl, Long> {

    public Optional<BlankUserMetadataImpl> findByUserAndMetadataKey(
        BlankUser user, String metadataKey);

    public Stream<BlankUserMetadataImpl> findByUser(BlankUser user);

    public Stream<BlankUserMetadataImpl> findByUser(BlankUser user, Sort sort);

    public Stream<BlankUserMetadataImpl> findByMetadataKey(String metadataKey);

    public Stream<BlankUserMetadataImpl> findByMetadataKey(String metadataKey,
        Sort sort);

}
