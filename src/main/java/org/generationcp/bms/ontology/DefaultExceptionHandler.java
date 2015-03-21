package org.generationcp.bms.ontology;

import org.generationcp.bms.exception.ApiRequestValidationException;
import org.generationcp.bms.ontology.dto.ErrorResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

@ControllerAdvice
public class DefaultExceptionHandler {

    @Autowired
    ResourceBundleMessageSource messageSource;

    @RequestMapping(produces = {APPLICATION_JSON_VALUE})
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleUncaughtException(Exception ex) throws IOException {
        ErrorResponse response = new ErrorResponse();

        if (ex.getCause() != null) {
            response.addError(ex.getCause().getMessage(), "SERVER");
        } else {
            response.addError(ex.getMessage(), "SERVER");
        }
        return response;
    }

    @RequestMapping(produces = {APPLICATION_JSON_VALUE})
    @ExceptionHandler(ApiRequestValidationException.class)
    @ResponseStatus(value = BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleValidationException(ApiRequestValidationException ex) throws IOException {

        ErrorResponse response = new ErrorResponse();

        for (ObjectError error : ex.getErrors()) {
            String message = messageSource.getMessage(error.getCode(), error.getArguments(), LocaleContextHolder.getLocale());
            if(error instanceof FieldError){
                FieldError fieldError = (FieldError) error;
                response.addError(message, fieldError.getField());
            } else {
                response.addError(message, error.getObjectName());
            }
        }
        return response;
    }
}
