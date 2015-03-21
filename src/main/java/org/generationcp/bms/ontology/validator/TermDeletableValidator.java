package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.TermRequest;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class TermDeletableValidator extends OntologyValidator implements org.springframework.validation.Validator{

    private static final Logger LOGGER = LoggerFactory.getLogger(TermDeletableValidator.class);

    @Autowired OntologyManagerService ontologyManagerService;

    @Autowired ResourceBundleMessageSource messageSource;

    @Autowired TermValidator termValidator;

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
            termValidator.validate(target, errors);

            if(errors.hasErrors()){
                return;
            }

            boolean isReferred = ontologyManagerService.isTermReferred(request.getId());
            if(!isReferred) return;

            addCustomError(errors, "id", CAN_NOT_DELETE_REFERRED_TERM, null);

        } catch (Exception e) {
            LOGGER.error("Error while validating object", e);
        }
    }
}
