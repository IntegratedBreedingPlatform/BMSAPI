package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import static org.generationcp.middleware.domain.oms.CvId.METHODS;


@Component
public class MethodEditableValidator extends BaseValidator implements org.springframework.validation.Validator{

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
            addCustomError(errors,"request", "should.not.be.null", null);
        }

        if(request != null){
            if(isNullOrEmpty(request.getName())){
                LOGGER.error("field should not be null");
                addCustomError(errors,"name", "should.not.be.null", null);
            }
            try {
                Method method = ontologyManagerService.getMethod(request.getId());
                if(method == null) {
                    LOGGER.error("term does not exist");
                    addCustomError(errors,"id","does.not.exist", new Object[]{request.getId()});
                }else {
                    if (ontologyManagerService.isTermReferred(request.getId())) {
                        if (!method.getName().trim().equals(request.getName().trim())) {
                            LOGGER.error("name not editable");
                            addCustomError(errors,"name", "name.not.editable", null);
                        }
                    }
                    Term methodByName = ontologyManagerService.getTermByNameAndCvId(request.getName(), METHODS.getId());
                    if(methodByName != null){
                        if((methodByName.getName().trim().equals(request.getName().trim())) && (methodByName.getId() != request.getId())){
                            LOGGER.debug("Method already exist with same name : " + request.getName());
                            addCustomError(errors,"name", "field.should.be.unique", null);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error while validating object", e);
            }
        }
    }
}
