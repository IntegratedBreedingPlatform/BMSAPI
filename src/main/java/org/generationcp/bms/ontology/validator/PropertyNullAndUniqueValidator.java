package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.PropertyRequest;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
import static org.generationcp.middleware.domain.oms.CvId.PROPERTIES;

@Component
public class PropertyNullAndUniqueValidator implements org.springframework.validation.Validator{

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyNullAndUniqueValidator.class);

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
            errors.rejectValue("request", formatErrorMessage(messageSource, "add.request.null", null));
        }

        if (request != null) {
            if(isNullOrEmpty(request.getName()) || request.getClasses().isEmpty()){
                if(isNullOrEmpty(request.getName())){
                    LOGGER.error("name should not be empty");
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", formatErrorMessage(messageSource, "should.not.be.null", null));
                }
                if(request.getClasses().isEmpty()){
                    LOGGER.error("one class required");
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "classes", formatErrorMessage(messageSource, "property.class.required", null));
                }

            } else {
                try {
                    Term property = ontologyManagerService.getTermByNameAndCvId(request.getName(), PROPERTIES.getId());

                    if (property != null) {
                        if (property.getName().trim().equals(request.getName().trim())) {
                            LOGGER.debug("Property already exist with same name : " + request.getName());
                            errors.rejectValue("name", formatErrorMessage(messageSource, "field.should.be.unique", null));
                        }
                    }
                    for(String className : request.getClasses()){
                        List<Property> propertyList = ontologyManagerService.getAllPropertiesWithClass(className);
                        if(propertyList.isEmpty()){
                            LOGGER.error("Class does not exist: " + className);
                            errors.rejectValue("classes", formatErrorMessage(messageSource, "property.class.invalid", new Object[]{className}));
                        }
                    }
                } catch (MiddlewareQueryException e) {
                    LOGGER.error("Error while validating object", e);
                }
            }
        }
    }
}
