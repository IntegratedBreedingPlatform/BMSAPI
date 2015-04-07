package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Objects;

/** Method add/update validator
 1 Name is required
 2 Name is unique
 3 Name cannot change if the method is already in use
 4 Name is no more than 200 characters
 5 Description is no more than 255 characters
 6 Name and description are textual
 */
@Component
public class MethodRequestValidator extends OntologyValidator implements org.springframework.validation.Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return MethodRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        shouldNotNullOrEmpty("request", target, errors);

        if(errors.hasErrors()) {
            return;
        }

        MethodRequest request = (MethodRequest) target;

        //3 Name cannot change if the method is already in use
        methodShouldBeEditable(request, errors);

        //Need to return from here because we should not check other constraints if request is not required to process
        if(errors.hasErrors()){
            return;
        }

        //1. Name is required
        shouldNotNullOrEmpty("name", request.getName(), errors);

        if(errors.hasErrors()){
            return;
        }

        //6. Name and description are textual
        shouldHaveValidString("name", request.getName(), errors);

        if(errors.hasErrors()){
            return;
        }

        //4. Name is no more than 200 characters
        nameShouldHaveMax200Chars("name", request.getName(), errors);

        //2. Name is unique
        checkTermUniqueness(request.getId(), request.getName(), CvId.METHODS.getId(), errors);

        //6. Name and description are textual
        shouldHaveValidString("description", request.getDescription(), errors);

        //5. Description is no more than 255 characters
        descriptionShouldHaveMax255Chars("description", request.getDescription(), errors);
    }

    private void methodShouldBeEditable(MethodRequest request, Errors errors){

        if(request.getId() == null){
            return;
        }

        try{

            Method oldMethod = ontologyManagerService.getMethod(request.getId());

            //that method should exist with requestId
            if(Objects.equals(oldMethod, null)){
                addCustomError(errors, TERM_DOES_NOT_EXIST, new Object[]{"method", request.getId()});
                return;
            }

            boolean isEditable = !ontologyManagerService.isTermReferred(request.getId());
            if(isEditable){
                return;
            }

            if(Objects.equals(request.getName(), oldMethod.getName())){
                return;
            }

        }catch (MiddlewareQueryException e){
            logError(e);
            addDefaultError(errors);
            return;
        }

        addCustomError(errors, "name" , TERM_NOT_EDITABLE, new Object[]{"method", "name"});
    }
}
