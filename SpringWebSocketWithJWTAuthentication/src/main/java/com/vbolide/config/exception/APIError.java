package com.vbolide.config.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Setter
@Getter
public class APIError {

	private int status;
	private String message;
	private String stacktrace;
	private List<ValidationError> validationErrors;


	public APIError(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public APIError(int status, String message, String stacktrace) {
		this.status = status;
		this.message = message;
		this.stacktrace = stacktrace;
	}


	@Getter
	@AllArgsConstructor
	private static class ValidationError{

		private String field;
		private List<String> messages;

	}

	public void addValidationError(String field, List<String> messages) {
		if(validationErrors == null) {
			validationErrors = new ArrayList<>();
		}
		validationErrors.add(new ValidationError(field, messages));
	}

}