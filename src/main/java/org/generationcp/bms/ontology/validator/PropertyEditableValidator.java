package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.PropertyRequest;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.generationcp.bms.util.I18nUtil.formatErrorMessage;


@Component
public class PropertyEditableValidator implements org.springframework.validation.Validator{

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyEditableValidator.class);

    @Autowired
    OntologyManagerService ontologyManagerService;
    @Autowired
    ResourceBundleMessageSource messageSource;

    @Override
    public boolean supports(Class<?> aClass) {
        return PropertyRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        PropertyRequest request = (PropertyRequest) target;

        if(request == null){
            errors.rejectValue("request", formatErrorMessage(messageSource, "request.null", null));
        }

        if(request != null){
            if(isNullOrEmpty(request.getName()) || request.getClasses().isEmpty()){
                if(isNullOrEmpty(request.getName())){
                    LOGGER.error("name should not be empty");
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", formatErrorMessage(messageSource, "should.not.be.null", null));
                }
                if(request.getClasses().isEmpty()){
                    LOGGER.error("one class required");
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "classes", formatErrorMessage(messageSource, "property.class.required", null));
                }

            }else {
                try {
                    Property property = ontologyManagerService.getProperty(request.getId());
                    if(property == null){
                        LOGGER.error("term does not exist");
                        errors.rejectValue("id", formatErrorMessage(messageSource, "does.not.exist", new Object[]{request.getId()}));
                    }else {
                        if(ontologyManagerService.isTermReferred(request.getId())){
                            if(!property.getName().trim().equals(request.getName().trim())){
                                LOGGER.error("name not editable");
                                errors.rejectValue("name", formatErrorMessage(messageSource, "name.not.editable", null));
                            }
                            for(String className : request.getClasses()){
                                List<Property> propertyList = ontologyManagerService.getAllPropertiesWithClass(className);
                                if(propertyList.isEmpty()){
                                    LOGGER.error("Class does not exist: " + className);
                                    errors.rejectValue("classes", formatErrorMessage(messageSource, "property.class.invalid", new Object[]{className}));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error while validating object", e);
                }
            }
        }
    }
}
