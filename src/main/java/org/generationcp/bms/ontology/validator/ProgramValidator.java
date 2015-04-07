package org.generationcp.bms.ontology.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ProgramValidator extends OntologyValidator implements Validator{
    @Override
    public boolean supports(Class<?> aClass) {
        return String.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        // check for program id should not be null
        shouldNotNullOrEmpty("id", target, errors);
        if(errors.hasErrors()) {
            return;
        }

        // check if program id is non numeric
        String id = (String) target;
        checkNumberFieldAndLength(id, errors);

        if(errors.hasErrors()) {
            return;
        }

        checkIfProgramExist("programId", id, errors);
    }
}
