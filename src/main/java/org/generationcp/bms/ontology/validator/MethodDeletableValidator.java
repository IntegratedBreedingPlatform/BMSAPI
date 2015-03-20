package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class MethodDeletableValidator extends BaseValidator implements org.springframework.validation.Validator{

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodDeletableValidator.class);

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
            addCustomError(errors, "id", "should.not.be.null", null);
            return;
        }
        try {
            Method method = ontologyManagerService.getMethod(id);
            if(method == null){
                LOGGER.error("term does not exist");
                addCustomError(errors, "id", "does.not.exist", new Object[]{id});
            }else {
                if(ontologyManagerService.isTermReferred(id)){
                    LOGGER.error("can not delete term, it is referred");
                    addCustomError(errors, "id", "delete.term.referred", null);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while validating object", e);
        }
    }
}
