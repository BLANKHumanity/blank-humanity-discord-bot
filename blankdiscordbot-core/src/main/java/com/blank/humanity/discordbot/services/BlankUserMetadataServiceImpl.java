package com.blank.humanity.discordbot.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blank.humanity.discordbot.database.BlankUserDao;
import com.blank.humanity.discordbot.database.BlankUserMetadataDao;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.user.BlankUserMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUserMetadataImpl;

import lombok.NonNull;
import reactor.core.publisher.Flux;

@Service
public class BlankUserMetadataServiceImpl implements BlankUserMetadataService {

    @Autowired
    private BlankUserMetadataDao metadataDao;

    @Autowired
    private BlankUserDao blankUserDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final PreparedStatementCreatorFactory fluxListMetadataKeysStatementFactory = new PreparedStatementCreatorFactory(
        "SELECT user_id, metadata_key, value FROM blank_user_metadata WHERE metadata_key = ?");

    static {
        fluxListMetadataKeysStatementFactory
            .addParameter(new SqlParameter(Types.VARCHAR));
    }

    @Transactional
    @Override
    public BlankUserMetadataImpl saveMetadata(@NonNull BlankUser user,
        @NonNull String key, @Nullable String value) {
        BlankUserMetadataImpl metadata = getMetadata(user, key)
            .map(BlankUserMetadataImpl.class::cast)
            .orElseGet(() -> BlankUserMetadataImpl
                .builder()
                .user(user)
                .metadataKey(key)
                .build());

        metadata.setValue(value);
        return metadataDao.save(metadata);
    }

    @Transactional
    @Override
    public Optional<BlankUserMetadata> getMetadata(BlankUser user, String key) {
        return metadataDao
            .findByUserAndMetadataKey(user, key)
            .map(BlankUserMetadata.class::cast);
    }

    @Transactional
    @Override
    public Stream<BlankUserMetadata> listUserMetadata(BlankUser user) {
        return metadataDao
            .findByUser(user)
            .map(BlankUserMetadata.class::cast);
    }

    @Transactional
    @Override
    public Stream<BlankUserMetadata> listAllMetadataByKey(String key) {
        return metadataDao
            .findByMetadataKey(key)
            .map(BlankUserMetadata.class::cast);
    }

    @Override
    public Flux<BlankUserMetadata> fluxListAllMetadataByKey(String key) {
        return Flux
            .defer(() -> Flux
                .fromStream(jdbcTemplate
                    .queryForStream(
                        fluxListMetadataKeysStatementFactory
                            .newPreparedStatementCreator(List.of(key)),
                        this::rowToMetadata)))
            .map(BlankUserMetadata.class::cast);
    }

    private BlankUserMetadataImpl rowToMetadata(ResultSet result, int rowNum)
        throws SQLException {
        return BlankUserMetadataImpl
            .builder()
            .metadataKey(result.getString("metadata_key"))
            .value(result.getString("value"))
            .user(
                blankUserDao.findById(result.getLong("user_id")).orElseThrow())
            .build();
    }

}
