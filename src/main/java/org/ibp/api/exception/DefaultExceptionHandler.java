
package org.ibp.api.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.apache.commons.io.FileUtils;
import org.generationcp.middleware.exceptions.MiddlewareRequestException;
import org.ibp.api.domain.common.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

@ControllerAdvice
public class DefaultExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionHandler.class);

	@Autowired
	ResourceBundleMessageSource messageSource;

	@Value("${multipart.maxFileSize}")
	private long maxFileSize;

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
	@ExceptionHandler(ApiRuntime2Exception.class)
	@ResponseStatus(value = INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorResponse handleApiRuntime2Exception(final ApiRuntime2Exception ex) {
		LOG.error("Error executing the API call.", ex);
		final ErrorResponse response = new ErrorResponse();
		final String message = this.getMessage(ex.getErrorCode(), ex.getParams());
		response.addError(message);
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(value = FORBIDDEN)
	@ResponseBody
	public ErrorResponse handleAccessDeniedException(AccessDeniedException ex) {
		LOG.error("Access Denied", ex);
		ErrorResponse response = new ErrorResponse();
		response.addError(getMessage("access.denied", null));
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse httpMessageNotReadableException(HttpMessageNotReadableException ex) {
		LOG.error("Error executing the API call.", ex);
		ErrorResponse response = new ErrorResponse();
		Throwable rootCause = ex.getRootCause();
		if (rootCause instanceof UnrecognizedPropertyException) {
			UnrecognizedPropertyException unrecognizedPropertyException = (UnrecognizedPropertyException) ex.getCause();
			response.addError(this.getMessage("not.recognised.field", new Object[] {unrecognizedPropertyException.getPropertyName()}));
		} else if (rootCause instanceof JsonParseException) {
			response.addError(this.getMessage("request.body.invalid", null));
		} else if (rootCause instanceof JsonMappingException) {
			response.addError(this.getMessage("request.body.invalid", null));
		}
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
		LOG.error("Error executing the API call.", ex);
		final ErrorResponse response = new ErrorResponse();
		DefaultExceptionHandler.LOG.error("Request not supported with given input", ex);
		response.addError(this.getMessage("request.method.not.supported", null));
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ApiRequestValidationException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse handleValidationException(ApiRequestValidationException ex) {
		LOG.error("Error executing the API call.", ex);
		final ErrorResponse response = buildErrorResponse(ex.getErrors());
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ApiValidationException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse handleApiValidationException(final ApiValidationException ex) {
		LOG.error("Error executing the API call.", ex);
		final ErrorResponse response = new ErrorResponse();
		final String message = this.getMessage(ex.getErrorCode(), ex.getParams());
		response.addError(message);
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(value = NOT_FOUND)
	@ResponseBody
	public ErrorResponse handleNotFoundException(ResourceNotFoundException ex) {
		LOG.error("Error executing the API call.", ex);
		final ErrorResponse response = new ErrorResponse();

		String message = this.getMessage(ex.getError().getCode(), ex.getError().getArguments());
		response.addError(message);

		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ForbiddenException.class)
	@ResponseStatus(value = FORBIDDEN)
	@ResponseBody
	public ErrorResponse handleForbiddenException(ForbiddenException ex) {
		LOG.error("Error executing the API call.", ex);
		final ErrorResponse response = new ErrorResponse();

		String message = this.getMessage(ex.getError().getCode(), ex.getError().getArguments());
		response.addError(message);

		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(NotSupportedException.class)
	@ResponseStatus(value = NOT_IMPLEMENTED)
	@ResponseBody
	public ErrorResponse handleNotSupportedException(NotSupportedException ex) {
		LOG.error("Error executing the API call.", ex);
		final ErrorResponse response = new ErrorResponse();

		final ObjectError error = ex.getError();
		String message = this.getMessage(error.getCode(), error.getArguments());
		response.addError(message);

		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(ConflictException.class)
	@ResponseStatus(value = CONFLICT)
	@ResponseBody
	public ErrorResponse handleConflictException(ConflictException ex) {
		LOG.error("Error executing the API call.", ex);
		final ErrorResponse response = buildErrorResponse(ex.getErrors());
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(MiddlewareRequestException.class)
	@ResponseStatus(value = BAD_REQUEST)
	@ResponseBody
	public ErrorResponse handleMiddlewareRequestException(MiddlewareRequestException ex) {
		LOG.error("Error executing the API call.", ex);
		final ErrorResponse response = new ErrorResponse();
		for (final Map.Entry<String, Object[]> error : ex.getErrorCodeParamsMultiMap().entries()) {
			response.addError(this.getMessage(error.getKey(), error.getValue()));
		}
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(PreconditionFailedException.class)
	@ResponseStatus(value = PRECONDITION_FAILED)
	@ResponseBody
	public ErrorResponse handlePreconditionFailedException(PreconditionFailedException ex) {
		LOG.error("Error executing the API call.", ex);
		final ErrorResponse response = buildErrorResponse(ex.getErrors());
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(BVDesignException.class)
	@ResponseStatus(value = INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorResponse handleBVDesignException(BVDesignException ex) {
		final ErrorResponse response = new ErrorResponse();
		DefaultExceptionHandler.LOG.error("BVDesign app failed to execute.", ex);
		response.addError(this.getMessage(ex.getBvErrorCode(), null));
		return response;
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(BVLicenseParseException.class)
	@ResponseStatus(value = INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorResponse handleBVLicenseParseException(final BVLicenseParseException ex) {
		final ErrorResponse response = new ErrorResponse();
		final String mainError = this.getMessage(ex.getBvErrorCode(), null);
		DefaultExceptionHandler.LOG.error("BVDesign license checking failed: " + mainError, ex);
		final StringBuilder sb = new StringBuilder(mainError);
		if (ex.getMessageFromApp() != null) {
			sb.append(ex.getMessageFromApp());
		}
		response.addError(sb.toString());
		return response;
	}

	@RequestMapping(produces = {MediaType.TEXT_PLAIN_VALUE})
	@ExceptionHandler(BrapiNotFoundException.class)
	@ResponseStatus(value = NOT_FOUND)
	@ResponseBody
	public String handleBrapiNotFoundException(final BrapiNotFoundException ex) {
		return "ERROR - " + Instant.now().toString() + " - " + ex.getMessage();
	}


	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(MultipartException.class)
	@ResponseStatus(value = PAYLOAD_TOO_LARGE)
	@ResponseBody
	public ErrorResponse handleMultipartException(final MultipartException ex) {
		final ErrorResponse response = new ErrorResponse();
		// FIXME not all MultipartException are because of max size (e.g file is not multipart)
		response.addError(this.getMessage("file.upload.too-large", new String[] {FileUtils.byteCountToDisplaySize(this.maxFileSize)}));
		return response;
	}

	private ErrorResponse buildErrorResponse(final List<ObjectError> objectErrors) {
		final ErrorResponse response = new ErrorResponse();
		for (ObjectError error : objectErrors) {
			String message = this.getMessage(error.getCode(), error.getArguments());
			if (error instanceof FieldError) {
				FieldError fieldError = (FieldError) error;
				response.addError(message, fieldError.getField());
			} else {
				response.addError(message);
			}
		}
		return response;
	}

	private String getMessage(final String code, final Object[] arguments) {
		return this.messageSource.getMessage(code, arguments, LocaleContextHolder.getLocale());
	}
}
