package org.generationcp.bms.ontology.validator;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.pojos.oms.CVTerm;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.*;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( { METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueField.UniqueFieldValidator.class)
@Documented
public @interface UniqueField {

    String message() default "{field.should.be.unique}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String fields() default "";

    public static class UniqueFieldValidator implements ConstraintValidator<UniqueField, String> {

        private static final Logger LOGGER = LoggerFactory.getLogger(UniqueFieldValidator.class);

        @Autowired
        OntologyManagerService ontologyManagerServices;

        UniqueField uniqueField;

        public void initialize(UniqueField uniqueFieldAnnotation) {
            this.uniqueField = uniqueFieldAnnotation;
        }

        public boolean isValid(String field, ConstraintValidatorContext constraintContext) {

            if(field.isEmpty()){
                LOGGER.debug("Field should not be empty");
                return true;
            }
            try {
                Term method = ontologyManagerServices.getTermByNameAndCvId(field, CvId.METHODS.getId());

                if(method != null) {
                    if (method.getName().trim().equals(field.trim())) {
                        LOGGER.debug("Method already exist with same name : " + field);
                        return false;
                    }
                }
            } catch (MiddlewareQueryException e) {
                LOGGER.error(e.getMessage());
            }
            constraintContext.disableDefaultConstraintViolation();
            constraintContext.buildConstraintViolationWithTemplate(uniqueField.message()).addConstraintViolation();
            return true;
        }

    }
}

