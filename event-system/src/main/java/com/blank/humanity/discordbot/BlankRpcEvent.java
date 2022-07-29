package com.blank.humanity.discordbot;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public abstract class BlankRpcEvent<R> extends BlankEvent {

    @JsonIgnore
    @Getter
    private Class<R> responseType;
    
    @JsonIgnore
    private R response;
    
    protected BlankRpcEvent(Class<R> responseType) {
	this.responseType = responseType;
    }
    
    public void setResponse(R response) {
	this.response = response;
    }

    public R getResponse(){
	return response;
    }

}