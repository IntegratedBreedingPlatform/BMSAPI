package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.MethodRequest;
import org.springframework.validation.Errors;

import org.springframework.stereotype.Component;

@Component
public class IntegerValidator extends BaseValidator implements org.springframework.validation.Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return MethodRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        String id = (String) target;
        checkNumber(id, errors);
    }
}
