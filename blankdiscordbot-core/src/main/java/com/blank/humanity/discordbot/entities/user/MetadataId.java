package com.blank.humanity.discordbot.entities.user;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataId implements Serializable {

    private static final long serialVersionUID = 2062777285281028186L;

    private long user;

    private String metadataKey;

}
