package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.bms.util.Init;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class IntegerValidator implements org.springframework.validation.Validator{

    @Autowired
    OntologyService ontologyService;
    @Autowired
    ResourceBundleMessageSource messageSource;

    @Override
    public boolean supports(Class<?> aClass) {
        return MethodRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        String id = (String) target;

        if(! id.matches("^[0-9]+$")){
            errors.rejectValue("id", Init.formatErrorMessage(messageSource, "should.be.numeric", null));
        }
    }
}
