package com.blank.humanity.discordbot.entities.user;

import java.util.Optional;
import java.util.function.Function;

public interface BlankUserMetadata {

    public BlankUser getUser();
    
    public String getMetadataKey();
    
    public Optional<String> getValue();
    
    public void setValue(String value);
    
    public default <T> Optional<T> map(Function<String, T> map){
        return getValue().map(map);
    }
    
    public default <T> Optional<T> flatMap(Function<String, Optional<T>> map){
        return getValue().flatMap(map);
    }
    
}
