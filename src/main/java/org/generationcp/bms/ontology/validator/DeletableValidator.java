package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.bms.util.Init;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class DeletableValidator implements org.springframework.validation.Validator{

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
            errors.rejectValue("id", Init.formatErrorMessage(messageSource, "should.not.be.null", null));
        }
        try {
            Method method = ontologyManagerService.getMethod(id);
            if(method == null){
                errors.rejectValue("id", Init.formatErrorMessage(messageSource, "does.not.exist", null));
            }else {
                if(ontologyManagerService.isTermReferred(id)){
                    errors.rejectValue("id", Init.formatErrorMessage(messageSource, "delete.term.referred", null));
                }
            }
        } catch (Exception e) {
        }
    }
}
