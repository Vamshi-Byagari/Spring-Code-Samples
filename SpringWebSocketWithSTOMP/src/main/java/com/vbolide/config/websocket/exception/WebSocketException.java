package com.vbolide.config.websocket.exception;

import lombok.Getter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Getter
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WebSocketException {

	private String type;
	private String destination;
	private String message;

	public WebSocketException(String destination, String message) {
		this.type = "error";
		this.destination = destination;
		this.message  = message;
	}
	
}