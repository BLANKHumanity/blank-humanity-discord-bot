package de.zorro909.blank.event;

import lombok.Getter;

public abstract class BlankEvent {

    @Getter
    private String eventIdentifier;

    protected BlankEvent() {
	this.eventIdentifier = getClass().getName();
    }

}
