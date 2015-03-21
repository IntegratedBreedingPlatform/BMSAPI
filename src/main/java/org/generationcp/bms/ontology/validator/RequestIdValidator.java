package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.MethodRequest;
import org.springframework.validation.Errors;

import org.springframework.stereotype.Component;

@Component
public class RequestIdValidator extends OntologyValidator implements org.springframework.validation.Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return MethodRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        shouldNotNullOrEmpty("id", target, errors);
        if(errors.hasErrors()) {
            return;
        }

        if(target instanceof Integer) return;

        String id = (String) target;
        checkNumberField("id", id, errors);
    }
}
