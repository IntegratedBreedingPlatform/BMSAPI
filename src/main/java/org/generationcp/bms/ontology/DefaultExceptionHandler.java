package org.generationcp.bms.ontology;

import org.apache.commons.lang.StringUtils;
import org.generationcp.bms.ontology.dto.ErrorResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class DefaultExceptionHandler {

    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ExceptionHandler({MissingServletRequestParameterException.class,
            UnsatisfiedServletRequestParameterException.class,
            HttpRequestMethodNotSupportedException.class,
            ServletRequestBindingException.class
    })
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody
    ErrorResponse handleRequestException(Exception ex) {
        ErrorResponse response = new ErrorResponse();
        response.addError(ex.getMessage(), "SERVER1");
        return response;
    }

    /*
        Map<String, Object>  map = Maps.newHashMap();
        map.put("error", "Data Integrity Error");
        map.put("cause", ex.getCause().getCause().getLocalizedMessage());
        return map;
     */
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public @ResponseBody
    ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException ex) throws IOException {
        ErrorResponse response = new ErrorResponse();
        response.addError(ex.getMessage(), "SERVER2");
        return response;
    }

    /*
        Map<String, Object>  map = Maps.newHashMap();
        map.put("error", "Data Error");
        map.put("cause", ex.getCause().getMessage());
        return map;
    */
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    ErrorResponse handleDataAccessException(DataAccessException ex) throws IOException {
        ErrorResponse response = new ErrorResponse();
        response.addError(ex.getMessage(), "SERVER3");
        return response;
    }

    /*
        Map<String, Object>  map = Maps.newHashMap();
        map.put("error", "Unsupported Media Type");
        map.put("cause", ex.getLocalizedMessage());
        map.put("supported", ex.getSupportedMediaTypes());
        return map;
     */
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public @ResponseBody
    ErrorResponse handleUnsupportedMediaTypeException(HttpMediaTypeNotSupportedException ex) throws IOException {
        ErrorResponse response = new ErrorResponse();
        response.addError(ex.getMessage(), "SERVER4");
        return response;
    }

    /*
        Map<String, Object>  map = Maps.newHashMap();
        map.put("error", "Unknown Error");
        if (ex.getCause() != null) {
            map.put("cause", ex.getCause().getMessage());
        } else {
            map.put("cause", ex.getMessage());
        }
        return map;
     */
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    ErrorResponse handleUncaughtException(Exception ex) throws IOException {
        ErrorResponse response = new ErrorResponse();

        System.out.println(ex.getMessage());
        ex.printStackTrace();

        if (ex.getCause() != null) {
            response.addError(ex.getCause().getMessage(), "SERVER");
        } else {
            response.addError(ex.getMessage(), "SERVER");
        }
        return response;
    }

    /*
        Map<String, Object>  violationMap = Maps.newHashMap();
        violationMap.put("value", constraintViolation.getInvalidValue());
        violationMap.put("type", constraintViolation.getRootBeanClass());
        violationMap.put("message", constraintViolation.getMessage());
        result.put(constraintViolation.getPropertyPath().toString(), violationMap);
    */
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody
    ErrorResponse handleValidationException(ConstraintViolationException ex) throws IOException {
        ErrorResponse response = new ErrorResponse();
        for (ConstraintViolation constraintViolation : ex.getConstraintViolations()) {
            response.addError(constraintViolation.getMessage(), constraintViolation.getRootBeanClass().getSimpleName());
        }
        return response;
    }

    /*
        Map<String, Object>  violationMap = Maps.newHashMap();
        violationMap.put("target", ex.getBindingResult().getTarget());
        violationMap.put("type", ex.getBindingResult().getTarget().getClass());
        violationMap.put("message", error.getDefaultMessage());
     */
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody
    ErrorResponse handleValidationException(MethodArgumentNotValidException ex) throws IOException {
        return parseErrors(ex.getBindingResult());
    }

    public static ErrorResponse parseErrors(BindingResult result){

        ErrorResponse response = new ErrorResponse();

        for (ObjectError error : result.getAllErrors()) {
            List<String> fields = new ArrayList<>();

            if(error instanceof FieldError){
                FieldError fieldError = (FieldError) error;
                response.addError(fieldError.getCodes()[fieldError.getCodes().length - 1], fieldError.getField());
                continue;
            }

            if(error.getArguments() == null) continue;

            for(Object o : error.getArguments()){
                if(!(o instanceof String)) {
                    continue;
                }

                String val = (String) o;
                if(!((String) o).startsWith("@Fields")) {
                    continue;
                }

                String[] strings = val.split(":")[1].split(",");
                for(String s : strings) {
                    if(StringUtils.isBlank(s)) {
                        continue;
                    }
                    fields.add(s);
                }
            }

            if(!fields.isEmpty()){
                response.addError(error.getDefaultMessage(), fields.toArray(new String[fields.size()]));
            }
        }
        return response;
    }
}
