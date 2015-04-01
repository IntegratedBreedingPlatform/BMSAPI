package org.generationcp.bms.ontology.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class RequestIdValidator extends OntologyValidator implements org.springframework.validation.Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return String.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        shouldNotNullOrEmpty("id", target, errors);
        if(errors.hasErrors()) {
            return;
        }

        if(target instanceof Integer){
            return;
        }

        String id = (String) target;
        checkNumberField("id", id, errors);
        if(errors.hasErrors()) {
            return;
        }
        checkMaximumLengthOfId("id", id, errors);
    }
}
