package com.blank.humanity.discordbot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public abstract class BlankEvent {

    @Getter
    @JsonIgnore
    private String eventIdentifier;

    protected BlankEvent() {
	this.eventIdentifier = getClass().getSimpleName();
    }

}
