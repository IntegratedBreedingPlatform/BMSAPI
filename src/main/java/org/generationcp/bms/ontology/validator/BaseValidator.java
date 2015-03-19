package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.util.I18nUtil;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

import static org.generationcp.bms.util.I18nUtil.formatErrorMessage;

public abstract class BaseValidator {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected OntologyManagerService ontologyManagerService;

    @Autowired
    ResourceBundleMessageSource messageSource;

    protected void checkNumber(String id, Errors errors){
        if(id.matches("^[0-9]+$")) return;
        log.error("field should be numeric");
        errors.rejectValue("id", I18nUtil.formatErrorMessage(messageSource, "should.be.numeric", null));
    }

    protected void checkTermExist(Integer id, Integer cvId, Errors errors){
        try {
            Term term = ontologyManagerService.getTermById(id);
            if(Objects.equals(term, null) || !Objects.equals(term.getVocabularyId(), cvId) ){
                errors.rejectValue("id", formatErrorMessage(messageSource, "does.not.exist", new Object[]{id.toString()}));
            }
        } catch (Exception e) {
            log.error("Error while validating object", e);
        }
    }

    protected void checkUniqueness(Integer id, String name, Integer cvId, Errors errors) {

        try {
            Term term = ontologyManagerService.getTermByNameAndCvId(name, cvId);
            if (term == null) return;

            if (Objects.isNull(id) || !Objects.equals(id, term.getId())) {
                errors.rejectValue("name", I18nUtil.formatErrorMessage(messageSource, "field.should.be.unique", null));
            }
        }
        catch (MiddlewareQueryException e) {
            log.error("Error checking uniqueness of term name", e);
        }
    }
}
