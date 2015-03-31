package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.TermRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;


@Component
public class TermValidator extends OntologyValidator implements org.springframework.validation.Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return TermRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TermRequest request = (TermRequest) target;
        checkTermExist(request.getTermName(), request.getId(), request.getCvId(), errors);
    }
}
