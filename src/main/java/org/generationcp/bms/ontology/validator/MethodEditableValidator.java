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

import static com.google.common.base.Strings.*;
import static org.generationcp.bms.util.I18nUtil.formatErrorMessage;


@Component
public class MethodEditableValidator implements org.springframework.validation.Validator{

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodEditableValidator.class);

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
            errors.rejectValue("request", formatErrorMessage(messageSource, "should.not.be.null", null));
        }

        if(request != null){
            if(isNullOrEmpty(request.getName())){
                LOGGER.error("field should not be null");
                errors.rejectValue("name", formatErrorMessage(messageSource, "should.not.be.null", null));
            }
            try {
                Method method = ontologyManagerService.getMethod(request.getId());
                if(method == null) {
                    LOGGER.error("term does not exist");
                    errors.rejectValue("id", formatErrorMessage(messageSource, "does.not.exist", null));
                }else {
                    if (ontologyManagerService.isTermReferred(request.getId())) {
                        if (!method.getName().trim().equals(request.getName().trim())) {
                            LOGGER.error("name not editable");
                            errors.rejectValue("name", formatErrorMessage(messageSource, "name.not.editable", null));
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error while validating object", e);
            }
        }
    }
}
