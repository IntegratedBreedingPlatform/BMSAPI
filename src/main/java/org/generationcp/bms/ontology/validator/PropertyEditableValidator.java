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

@Component
public class PropertyEditableValidator extends BaseValidator implements org.springframework.validation.Validator{

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
            addCustomError(errors, "request", "request.null", null);
        }
         //todo : i have removed null as last parameter from  ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name","should.not.be.null");
        if(request != null){
            if(isNullOrEmpty(request.getName()) || request.getClasses().isEmpty()){
                if(isNullOrEmpty(request.getName())){
                    LOGGER.error("name should not be empty");
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "should.not.be.null");
                }
                if(request.getClasses().isEmpty()){
                    LOGGER.error("one class required");
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "classes","property.class.required");
                }

            }else {
                try {
                    Property property = ontologyManagerService.getProperty(request.getId());
                    if(property == null){
                        LOGGER.error("term does not exist");
                        addCustomError(errors,"id","does.not.exist", new Object[]{request.getId()});
                    }else {
                        if(ontologyManagerService.isTermReferred(request.getId())){
                            if(!property.getName().trim().equals(request.getName().trim())){
                                LOGGER.error("name not editable");
                                addCustomError(errors,"name","name.not.editable", null);
                            }
                            for(String className : request.getClasses()){
                                List<Property> propertyList = ontologyManagerService.getAllPropertiesWithClass(className);
                                if(propertyList.isEmpty()){
                                    LOGGER.error("Class does not exist: " + className);
                                    addCustomError(errors,"classes","property.class.invalid", new Object[]{className});
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
