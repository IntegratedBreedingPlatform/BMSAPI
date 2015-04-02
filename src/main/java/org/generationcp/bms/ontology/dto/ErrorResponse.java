package org.generationcp.bms.ontology.dto;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {

    private List<Error> errors;

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public void addError(String message, String... fields){

        if(this.errors == null){
            this.errors = new ArrayList<>();
        }

        this.errors.add(new Error(fields, message));
    }

    public static class Error{

        private String[] fieldNames;
        private String message;

        public Error(String[] fieldNames, String message){
            this.fieldNames = fieldNames;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String[] getFieldNames() {
            return fieldNames;
        }

        public void setFieldNames(String[] fieldNames) {
            this.fieldNames = fieldNames;
        }
    }
}
