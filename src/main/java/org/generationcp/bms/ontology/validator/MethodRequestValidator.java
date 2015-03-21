package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class MethodRequestValidator extends OntologyValidator implements org.springframework.validation.Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return MethodRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        MethodRequest request = (MethodRequest) target;

        if(request == null){
            addCustomError(errors,"request","should.not.be.null", null);
            return;
        }

        if(isNullOrEmpty(request.getName())){
            addCustomError(errors,"name", "should.not.be.null", null);
            return;
        }

        try {
            Term method = ontologyManagerService.getTermByNameAndCvId(request.getName(), CvId.METHODS.getId());

            if (method != null) {
                if (method.getName().trim().equals(request.getName().trim())) {
                    addCustomError(errors,"name", "field.should.be.unique", null);
                }
            }
        } catch (MiddlewareQueryException e) {
            log.error("Error while validating object", e);
        }
    }
}
