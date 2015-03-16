package org.generationcp.bms.ontology;

import org.generationcp.bms.ontology.dto.ErrorResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.net.BindException;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ControllerAdvice
public class DefaultExceptionHandler {

    private final String SERVER = "SERVER";

    @RequestMapping(produces = {APPLICATION_JSON_VALUE})
    @ExceptionHandler({MissingServletRequestParameterException.class,
            UnsatisfiedServletRequestParameterException.class,
            HttpRequestMethodNotSupportedException.class,
            ServletRequestBindingException.class
    })
    @ResponseStatus(value = BAD_REQUEST)
    public @ResponseBody
    ErrorResponse handleRequestException(Exception ex) {
        ErrorResponse response = new ErrorResponse();
        response.addError(ex.getMessage(), SERVER);
        return response;
    }

    @RequestMapping(produces = {APPLICATION_JSON_VALUE})
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = CONFLICT)
    @ResponseBody
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException ex) throws IOException {
        ErrorResponse response = new ErrorResponse();
        response.addError(ex.getMessage(), SERVER);
        return response;
    }

    @RequestMapping(produces = {APPLICATION_JSON_VALUE})
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleDataAccessException(DataAccessException ex) throws IOException {
        ErrorResponse response = new ErrorResponse();
        response.addError(ex.getMessage(), SERVER);
        return response;
    }

    @RequestMapping(produces = {APPLICATION_JSON_VALUE})
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(value = UNSUPPORTED_MEDIA_TYPE)
    @ResponseBody
    public ErrorResponse handleUnsupportedMediaTypeException(HttpMediaTypeNotSupportedException ex) throws IOException {
        ErrorResponse response = new ErrorResponse();
        response.addError(ex.getMessage(), SERVER);
        return response;
    }

    @RequestMapping(produces = {APPLICATION_JSON_VALUE})
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleUncaughtException(Exception ex) throws IOException {
        ErrorResponse response = new ErrorResponse();

        if (ex.getCause() != null) {
            response.addError(ex.getCause().getMessage(), SERVER);
        } else {
            response.addError(ex.getMessage(), SERVER);
        }
        return response;
    }

    @RequestMapping(produces = {APPLICATION_JSON_VALUE})
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleValidationException(ConstraintViolationException ex) throws IOException {
        ErrorResponse response = new ErrorResponse();
        for (ConstraintViolation constraintViolation : ex.getConstraintViolations()) {
            response.addError(constraintViolation.getMessage(), constraintViolation.getRootBeanClass().getSimpleName());
        }
        return response;
    }

    @RequestMapping(produces = {APPLICATION_JSON_VALUE})
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(value = BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) throws IOException {
        return parseErrors(ex.getBindingResult());
    }

    public static ErrorResponse parseErrors(BindingResult result){
        ErrorResponse response = new ErrorResponse();

        for (ObjectError error : result.getAllErrors()) {
            if(error instanceof FieldError){
                FieldError fieldError = (FieldError) error;
                response.addError(fieldError.getCodes()[fieldError.getCodes().length - 1], fieldError.getField());
            }
        }
        return response;
    }
}
