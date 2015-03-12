package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.bms.util.Init;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;


@Component
public class EditableValidator implements org.springframework.validation.Validator{

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

        MethodRequest request = (MethodRequest) target;

        if(request == null){
            errors.rejectValue("request", Init.formatErrorMessage(messageSource, "should.not.be.null", null));
        }

        assert request != null;

        if(request.getName().isEmpty()){
            errors.rejectValue("name", Init.formatErrorMessage(messageSource, "should.not.be.null", null));
        }
        try {
            Method method = ontologyManagerService.getMethod(request.getId());
            if(method == null) {
                errors.rejectValue("id", Init.formatErrorMessage(messageSource, "does.not.exist", null));
            }else {
                if (ontologyManagerService.isTermReferred(request.getId())) {
                    if (!method.getName().trim().equals(request.getName().trim())) {
                        errors.rejectValue("name", Init.formatErrorMessage(messageSource, "name.not.editable", null));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
