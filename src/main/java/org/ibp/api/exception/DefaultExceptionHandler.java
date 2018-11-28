
package org.ibp.api.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

import org.generationcp.middleware.exceptions.MiddlewareRequestException;
import org.ibp.api.domain.common.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

@ControllerAdvice
public class DefaultExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionHandler.class);

	@Autowired
	ResourceBundleMessageSource messageSource;

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorResponse handleUncaughtException(Exception ex) {
		LOG.error("Error executing the API call.", ex);
		ErrorResponse response = new ErrorResponse();
		if (ex.getCause() != null) {
			response.addError(ex.getCause().getMessage());
		} else {
			response.addError(ex.getMessage());
		}
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse httpMessageNotReadableException(HttpMessageNotReadableException ex) {
		ErrorResponse response = new ErrorResponse();
		Throwable rootCause = ex.getRootCause();
		if (rootCause instanceof UnrecognizedPropertyException) {
			UnrecognizedPropertyException unrecognizedPropertyException = (UnrecognizedPropertyException) ex.getCause();
			response.addError(this.messageSource.getMessage("not.recognised.field",
					new Object[] {unrecognizedPropertyException.getPropertyName()}, LocaleContextHolder.getLocale()));
		} else if (rootCause instanceof JsonParseException) {
			response.addError(this.messageSource.getMessage("request.body.invalid", null, LocaleContextHolder.getLocale()));
		} else if (rootCause instanceof JsonMappingException) {
			response.addError(this.messageSource.getMessage("request.body.invalid", null, LocaleContextHolder.getLocale()));
		}
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
		ErrorResponse response = new ErrorResponse();
		DefaultExceptionHandler.LOG.error("Request not supported with given input", ex);
		response.addError(this.messageSource.getMessage("request.method.not.supported", null, LocaleContextHolder.getLocale()));
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ApiRequestValidationException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse handleValidationException(ApiRequestValidationException ex) {

		ErrorResponse response = new ErrorResponse();

		for (ObjectError error : ex.getErrors()) {
			String message = this.messageSource.getMessage(error.getCode(), error.getArguments(), LocaleContextHolder.getLocale());
			if (error instanceof FieldError) {
				FieldError fieldError = (FieldError) error;
				response.addError(message, fieldError.getField());
			} else {
				response.addError(message);
			}
		}
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(value = NOT_FOUND)
	@ResponseBody
	public ErrorResponse handleNotFoundException(ResourceNotFoundException ex) {

		ErrorResponse response = new ErrorResponse();

		String message = this.messageSource.getMessage(ex.getError().getCode(), ex.getError().getArguments(), LocaleContextHolder.getLocale());
		response.addError(message);

		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ForbiddenException.class)
	@ResponseStatus(value = FORBIDDEN)
	@ResponseBody
	public ErrorResponse handleForbiddenException(ForbiddenException ex) {

		ErrorResponse response = new ErrorResponse();

		String message = this.messageSource.getMessage(ex.getError().getCode(), ex.getError().getArguments(), LocaleContextHolder.getLocale());
		response.addError(message);

		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(NotSupportedException.class)
	@ResponseStatus(value = NOT_IMPLEMENTED)
	@ResponseBody
	public ErrorResponse handleNotSupportedException(NotSupportedException ex) {

		ErrorResponse response = new ErrorResponse();

		String message = this.messageSource.getMessage(ex.getError().getCode(), ex.getError().getArguments(), LocaleContextHolder.getLocale());
		response.addError(message);

		return response;
	}


	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ConflictException.class)
	@ResponseStatus(value = CONFLICT)
	@ResponseBody
	public ErrorResponse handleConflictException(ConflictException ex) {

		ErrorResponse response = new ErrorResponse();

		for (ObjectError error : ex.getErrors()) {
			String message = this.messageSource.getMessage(error.getCode(), error.getArguments(), LocaleContextHolder.getLocale());
			if (error instanceof FieldError) {
				FieldError fieldError = (FieldError) error;
				response.addError(message, fieldError.getField());
			} else {
				response.addError(message);
			}
		}
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(MiddlewareRequestException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse handleUncaughtException(MiddlewareRequestException ex) {
		ErrorResponse response = new ErrorResponse();
		response.addError(ex.getMessage());
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(PreconditionFailedException.class)
	@ResponseStatus(value = PRECONDITION_FAILED)
	@ResponseBody
	public ErrorResponse handleConflictException(PreconditionFailedException ex) {

		ErrorResponse response = new ErrorResponse();

		for (ObjectError error : ex.getErrors()) {
			String message = this.messageSource.getMessage(error.getCode(), error.getArguments(), LocaleContextHolder.getLocale());
			if (error instanceof FieldError) {
				FieldError fieldError = (FieldError) error;
				response.addError(message, fieldError.getField());
			} else {
				response.addError(message);
			}
		}
		return response;
	}

}
