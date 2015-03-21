package org.generationcp.bms.ontology.validator;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import org.springframework.validation.Errors;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.generationcp.middleware.domain.oms.CvId.METHODS;

@Component
public class MethodRequestValidator extends BaseValidator implements org.springframework.validation.Validator{

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodRequestValidator.class);

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
            addCustomError(errors,"request","should.not.be.null", null);
        }

        if (request != null) {
            if(isNullOrEmpty(request.getName())){
                LOGGER.error("field should not be null");
                addCustomError(errors,"name", "should.not.be.null", null);
            }else {
                try {
                    Term method = ontologyManagerService.getTermByNameAndCvId(request.getName(), METHODS.getId());

                    if (method != null) {
                        if (method.getName().trim().equals(request.getName().trim())) {
                            LOGGER.debug("Method already exist with same name : " + request.getName());
                            addCustomError(errors,"name", "field.should.be.unique", null);
                        }
                    }
                } catch (MiddlewareQueryException e) {
                    LOGGER.error("Error while validating object", e);
                }
            }
        }
    }
}
