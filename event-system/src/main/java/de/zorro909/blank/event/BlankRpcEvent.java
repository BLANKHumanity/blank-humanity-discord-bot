package de.zorro909.blank.event;

import lombok.Getter;

public abstract class BlankRpcEvent<R> extends BlankEvent {

    @Getter
    private transient Class<R> responseType;
    
    private transient R response;
    
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
