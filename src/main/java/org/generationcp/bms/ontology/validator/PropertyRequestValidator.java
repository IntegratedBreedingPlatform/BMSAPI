package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.PropertyRequest;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.OntologyProjections;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.util.Util;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Request validator for add/edit property
 1 Name is required
 2 Name is unique
 3 Classes must be an array containing at least one string
 4 Each class should contain unique valid value
 5 Name cannot change if the property is already in use
 6 Name is no more than 200 characters
 7 Description is no more than 255 characters
 */
@Component
public class PropertyRequestValidator extends OntologyValidator implements org.springframework.validation.Validator{

    static final String DUPLICATE_ENTRIES_IN_CLASSES = "property.class.duplicate.entries";
    static final String INVALID_CLASS_NAME = "property.class.name.not.found";
    static final String PROPERTY_NOT_EDITABLE = "property.not.editable";

    @Override
    public boolean supports(Class<?> aClass) {
        return PropertyRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        shouldNotNullOrEmpty("request", target, errors);

        if(errors.hasErrors()) {
            return;
        }

        PropertyRequest request = (PropertyRequest) target;

        //3 Name cannot change if the method is already in use
        propertyShouldBeEditable(request, errors);

        //Need to return from here because we should not check other constraints if request is not required to process
        if(errors.hasErrors()){
            return;
        }

        //1. Name is required
        shouldNotNullOrEmpty("name", request.getName(), errors);

        //6. Name is no more than 200 characters
        nameShouldHaveMax200Chars("name", request.getName(), errors);

        //2. Name is unique
        checkTermUniqueness(request.getId(), request.getName(), CvId.PROPERTIES.getId(), errors);

        //7. Description is no more than 255 characters
        descriptionShouldHaveMax255Chars("description", request.getDescription(), errors);

        //3. Classes must be an array containing at least one string
        shouldNotNullOrEmpty("classes", request.getClasses(), errors);

        //Need to return from here because we should not check other constraints if request is not required to process
        if (errors.hasErrors()){
            return;
        }

        //4 Each class should contain unique valid value
        shouldClassesContainValidValue(request.getClasses(), errors);
    }

    private void shouldClassesContainValidValue(List<String> classes, Errors errors){

        //Convert to set to check duplication
        Set<String> classesSet = new HashSet<>(classes);

        //If both size are same then there is no duplication of classes
        if(classesSet.size() != classes.size()){
            addCustomError(errors, "classes", DUPLICATE_ENTRIES_IN_CLASSES, null);
            return;
        }

        //Trying to see for valid class names
        try {

            List<String> validClassNames = Util.convertAll(ontologyManagerService.getAllTraitClass(), OntologyProjections.termNameProjection);

            for(int i = 0; i < classes.size(); i++) {
                if(!validClassNames.contains(classes.get(i))){
                    addCustomError(errors, "classes[" + i + "]", INVALID_CLASS_NAME, new Object[]{classes.get(i)});
                }
            }

        } catch (MiddlewareQueryException e) {
            logError(e);
        }
    }

    private void propertyShouldBeEditable(PropertyRequest request, Errors errors){
        if(request.getId() == null){
            return;
        }

        try{

            Property oldProperty = ontologyManagerService.getProperty(request.getId());

            //that property should exist with requestId
            if(Objects.equals(oldProperty, null)){
                addCustomError(errors, DOES_NOT_EXIST, new Object[]{request.getId()});
                return;
            }

            boolean isEditable = !ontologyManagerService.isTermReferred(request.getId());
            if(isEditable){
                return;
            }

            if(Objects.equals(request.getName(), oldProperty.getName())
                    && Objects.equals(request.getClasses().size(), oldProperty.getClassNames().size())
                    && request.getClasses().containsAll(oldProperty.getClassNames())) {
                return;
            }

        }catch (Exception e){
            logError(e);
            addDefaultError(errors);
            return;
        }

        errors.reject(PROPERTY_NOT_EDITABLE);
    }
}
