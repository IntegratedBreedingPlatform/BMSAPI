package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import static org.generationcp.bms.util.I18nUtil.formatErrorMessage;

@Component
public class PropertyDeletableValidator implements org.springframework.validation.Validator{

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyDeletableValidator.class);

    @Autowired
    OntologyManagerService ontologyManagerService;
    @Autowired
    ResourceBundleMessageSource messageSource;

    @Override
    public boolean supports(Class<?> aClass) {
        return MethodRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        Integer id = (Integer) target;

        if(id == null){
            LOGGER.error("id should not be null");
            errors.rejectValue("id", formatErrorMessage(messageSource, "should.not.be.null", null));
        }
        try {
            Property property = ontologyManagerService.getProperty(id);
            if(property == null){
                LOGGER.error("term does not exist");
                errors.rejectValue("id", formatErrorMessage(messageSource, "does.not.exist", new Object[]{id}));
            }else {
                if(ontologyManagerService.isTermReferred(id)){
                    LOGGER.error("can not delete term, it is referred");
                    errors.rejectValue("id", formatErrorMessage(messageSource, "delete.term.referred", null));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while validating object", e);
        }
    }
}
