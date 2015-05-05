package org.ibp.api.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.ibp.api.domain.common.ErrorResponse;
import org.ibp.api.domain.common.ValidationErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class DefaultExceptionHandler {

  	private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionHandler.class);

	@Autowired
	ResourceBundleMessageSource messageSource;

	@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorResponse handleUncaughtException(Exception ex) {
		ErrorResponse response = new ErrorResponse();
		if (ex.getCause() != null) {
			response.addError(ex.getCause().getMessage());
		} else {
			response.addError(ex.getMessage());
		}
		return response;
	}

	@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse httpMessageNotReadableException(HttpMessageNotReadableException ex) {
		ErrorResponse response = new ErrorResponse();
		Throwable rootCause = ex.getRootCause();
		if (rootCause instanceof UnrecognizedPropertyException) {
			UnrecognizedPropertyException unrecognizedPropertyException = (UnrecognizedPropertyException) ex.getCause();
			response.addError(this.messageSource.getMessage("not.recognised.field", new Object[]{unrecognizedPropertyException.getPropertyName()}, LocaleContextHolder.getLocale()));
		} else if (rootCause instanceof JsonParseException) {
			response.addError(this.messageSource.getMessage("invalid.body", null, LocaleContextHolder.getLocale()));
		} else if (rootCause instanceof JsonMappingException) {
			response.addError(this.messageSource.getMessage("invalid.body", null, LocaleContextHolder.getLocale()));
		}
		return response;
	}

	@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
		ErrorResponse response = new ErrorResponse();
	  	LOG.error("Request not supported with given input", ex);
		response.addError(this.messageSource.getMessage("request.method.not.supported", null, LocaleContextHolder.getLocale()));
		return response;
	}

	@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@ExceptionHandler(ApiRequestValidationException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse handleValidationException(ApiRequestValidationException ex) {

		ErrorResponse response = new ErrorResponse();

		for (ObjectError error : ex.getErrors()) {
			String message = this.messageSource.getMessage(error.getCode(), error.getArguments(),
					LocaleContextHolder.getLocale());
			if (error instanceof FieldError) {
				FieldError fieldError = (FieldError) error;
				response.addError(message, fieldError.getField());
			} else {
				response.addError(message);
			}
		}
		return response;
	}

	@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	@ExceptionHandler(ApiRequestValidationException2.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse requestValidationExceptionHandler(ApiRequestValidationException2 ex) {
		ErrorResponse response = new ErrorResponse();

		ValidationErrors validationErrors = ex.getValidationObject();

		for (ValidationErrors.ErrorLine errorLine : validationErrors.getErrors()) {

			Object[] arguments = new Object[errorLine.getTermNames().size() + errorLine.getValues().size()];

			Integer termLen = errorLine.getTermNames().size();
			Integer valLen = errorLine.getValues().size();

			for(int a=0; a < termLen; a++){
				String term = errorLine.getTermNames().get(a);
				arguments[a] = this.messageSource.getMessage(term, null, term, LocaleContextHolder.getLocale());
			}

			for(int v=0; v < valLen; v++){
				arguments[termLen + v] = errorLine.getValues().get(v);
			}

			String[] fieldNames = errorLine.getFieldNames().toArray(new String[errorLine.getFieldNames().size()]);
			response.addError(this.messageSource.getMessage(errorLine.getTemplate(), arguments, LocaleContextHolder.getLocale()), fieldNames);
		}

		return response;
	}
}
