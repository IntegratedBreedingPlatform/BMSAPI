package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.PropertyRequest;
import org.generationcp.middleware.domain.oms.CvId;
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

import static org.generationcp.middleware.domain.oms.CvId.PROPERTIES;

@Component
public class PropertyNullAndUniqueValidator extends OntologyValidator implements org.springframework.validation.Validator{

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
            addCustomError(errors, "request", "request.null", null);
        }

        if (request != null) {
            if(isNullOrEmpty(request.getName()) || request.getClasses().isEmpty()){
                if(isNullOrEmpty(request.getName())){
                    LOGGER.error("name should not be empty");
                    addCustomError(errors, "name", "should.not.be.null", null);

                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name","should.not.be.null");
                }
                if(request.getClasses().isEmpty()){
                    LOGGER.error("one class required");
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "classes",  "property.class.required");
                }

            } else {
                try {
                    Term property = ontologyManagerService.getTermByNameAndCvId(request.getName(), PROPERTIES.getId());

                    checkTermUniqueness(property.getId(), property.getName(), CvId.PROPERTIES.getId(), errors);

                    for(String className : request.getClasses()){
                        List<Property> propertyList = ontologyManagerService.getAllPropertiesWithClass(className);
                        if(propertyList.isEmpty()){
                            LOGGER.error("Class does not exist: " + className);
                            addCustomError(errors, "classes", "property.class.invalid", new Object[]{className});
                        }
                    }
                } catch (MiddlewareQueryException e) {
                    LOGGER.error("Error while validating object", e);
                }
            }
        }
    }
}
