package com.vbolide.config.websocket.exception;

import lombok.Getter;

@Getter
public class AuthenticationFailureException extends IllegalStateException{

	private static final long serialVersionUID = 5462830237458333278L;

	private final int code;
	private final String message;

	public AuthenticationFailureException(int code, String message) {
		this.code = code;
		this.message = message;
	}

}