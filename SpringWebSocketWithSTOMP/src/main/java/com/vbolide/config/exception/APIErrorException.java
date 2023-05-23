package com.vbolide.config.exception;

import lombok.Getter;

@Getter
public class APIErrorException extends RuntimeException{

	private static final long serialVersionUID = -148259138532252358L;

	private final APIError apiError;

	public APIErrorException(APIError apiError) {
		super(apiError.getMessage());
		this.apiError = apiError;
	}

}