package com.vbolide.config.exception;

import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class APIExceptionHandler extends ResponseEntityExceptionHandler{

	@Value("${api.error.include-stacktrace}")
	private boolean includeStacktrace;


	/** Handling Spring thrown Exceptions **/

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		return handleBindException(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleBindException(
		BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

		APIError apiError = new APIError(status.value(), "validation errors");
		addStacktrace(request, ex, apiError);

		ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.collect(Collectors.groupingBy(FieldError::getField))
				.forEach((key, value) ->
					apiError.addValidationError(key, value.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList()))
				);

		return ResponseEntity.status(status).body(apiError);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
		MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		return buildAPIError(status, ex, request);
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(
		NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

		System.out.printf("APIExceptionHandler::handleNoHandlerFoundException: ex: %s, message: %s%n", ex.getClass().getSimpleName(), ex.getLocalizedMessage());
		return buildAPIError(status, "resource doesn't exists", ex, request);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
		Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {

		System.out.printf("APIExceptionHandler::handleExceptionInternal: ex: %s, message: %s%n", ex.getClass().getSimpleName(), ex.getLocalizedMessage());
		return buildAPIError(status, "unknown error occured", ex, request);
	}

	/** Handling Application thrown Exceptions **/

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleAllOtherExceptions(Exception ex) {

		System.out.printf("APIExceptionHandler::handleAllOtherExceptions: ex: %s, message: %s%n", ex.getClass().getSimpleName(), ex.getLocalizedMessage());
		return buildAPIError(ex instanceof NullPointerException ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR, "unknown error occured");
	}

	@ExceptionHandler(APIErrorException.class)
	public ResponseEntity<Object> handleAPIErrorException(APIErrorException ex){

		System.out.printf("APIExceptionHandler::handleAllOtherExceptions: ex: %s, message: %s%n", ex.getClass().getSimpleName(), ex.getLocalizedMessage());
		return ResponseEntity.status(ex.getApiError().getStatus()).body(ex.getApiError());
	}


	/** Helper methods to build APIError **/

	private ResponseEntity<Object> buildAPIError(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(new APIError(status.value(), message));
	}

	private ResponseEntity<Object> buildAPIError(HttpStatus status, Exception ex, WebRequest webRequest) {
		APIError apiError = new APIError(status.value(), ex.getLocalizedMessage());
		addStacktrace(webRequest, ex, apiError);
		return ResponseEntity.status(status).body(apiError);
	}
	
	private ResponseEntity<Object> buildAPIError(HttpStatus status, String message, Exception ex, WebRequest webRequest) {
		APIError apiError = new APIError(status.value(), message);
		addStacktrace(webRequest, ex, apiError);
		return ResponseEntity.status(status).body(apiError);
	}

	private void addStacktrace(WebRequest webRequest, Exception ex, APIError apiError) {
		if(includeStacktrace && hasStacktrace(webRequest)) {
			apiError.setStacktrace(ExceptionUtils.getStackTrace(ex));
		}
	}

	private boolean hasStacktrace(WebRequest webRequest) {
		String[] values = webRequest.getParameterValues("trace");
		return Objects.nonNull(values) && values.length > 0 && values[0].contentEquals("true");
	}

}