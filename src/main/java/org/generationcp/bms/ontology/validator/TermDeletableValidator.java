package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.TermRequest;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class TermDeletableValidator extends OntologyValidator implements org.springframework.validation.Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return TermRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TermRequest request = (TermRequest) target;

        if(request == null){
            addCustomError(errors, SHOULD_NOT_NULL_OR_EMPTY, null);
            return;
        }

        try {
            checkTermExist(request.getTermName(), request.getId(), request.getCvId(), errors);

            if(errors.hasErrors()){
                return;
            }

            boolean isReferred = ontologyManagerService.isTermReferred(request.getId());
            if(!isReferred){
                return;
            }

            addCustomError(errors, "id", CAN_NOT_DELETE_REFERRED_TERM, null);

        } catch (MiddlewareQueryException e) {
            log.error("Error while validating object", e);
        }
    }
}
