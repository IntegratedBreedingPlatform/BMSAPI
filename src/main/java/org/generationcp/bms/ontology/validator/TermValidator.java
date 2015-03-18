package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.TermRequest;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Objects;

import static org.generationcp.bms.util.I18nUtil.formatErrorMessage;


@Component
public class TermValidator implements org.springframework.validation.Validator{

    private static final Logger LOGGER = LoggerFactory.getLogger(TermValidator.class);

    @Autowired
    OntologyManagerService ontologyManagerService;

    @Autowired
    ResourceBundleMessageSource messageSource;

    @Override
    public boolean supports(Class<?> aClass) {
        return TermRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        TermRequest request = (TermRequest) target;

        if(request == null){
            errors.rejectValue("request", formatErrorMessage(messageSource, "should.not.be.null", null));
        } else {
            try {
                Term term = ontologyManagerService.getTermById(request.getId());
                if(Objects.equals(term, null) || term.getVocabularyId() != request.getCvId()){
                    errors.rejectValue("id", formatErrorMessage(messageSource, "does.not.exist", new Object[]{request.getId()}));
                }
            } catch (Exception e) {
                LOGGER.error("Error while validating object", e);
            }
        }
    }
}
