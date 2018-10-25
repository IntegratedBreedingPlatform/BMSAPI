
package org.ibp.api.exception;

import org.ibp.api.domain.common.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@ControllerAdvice
public class TestExceptionHandler {

	/*
	 * FIXME Find a better way to handle exceptions in test
	 * reusing default handler for now
	 */
	@Autowired
	private DefaultExceptionHandler defaultExceptionHandler;

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorResponse handleUncaughtException(Exception ex) {
		return this.defaultExceptionHandler.handleUncaughtException(ex);
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse httpMessageNotReadableException(HttpMessageNotReadableException ex) {
		return this.defaultExceptionHandler.httpMessageNotReadableException(ex);
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
		return this.defaultExceptionHandler.httpRequestMethodNotSupportedException(ex);
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ApiRequestValidationException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse handleValidationException(ApiRequestValidationException ex) {
		return this.defaultExceptionHandler.handleValidationException(ex);
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(value = NOT_FOUND)
	@ResponseBody
	public ErrorResponse handleNotFoundException(ResourceNotFoundException ex) {
		return this.defaultExceptionHandler.handleNotFoundException(ex);
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ForbiddenException.class)
	@ResponseStatus(value = FORBIDDEN)
	@ResponseBody
	public ErrorResponse handleForbiddenException(ForbiddenException ex) {
		return this.defaultExceptionHandler.handleForbiddenException(ex);
	}
}
