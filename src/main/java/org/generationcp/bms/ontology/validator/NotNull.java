package org.generationcp.bms.ontology.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( { METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = NotNull.NotNullValidator.class)
@Documented
public @interface NotNull {

    String message() default "{field.should.not.empty}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String fields() default "";

    public static class NotNullValidator implements ConstraintValidator<NotNull, String> {

        private static final Logger LOGGER = LoggerFactory.getLogger(NotNullValidator.class);

        NotNull notNull;

        public void initialize(NotNull notNullAnnotation) {
            this.notNull = notNullAnnotation;
        }

        public boolean isValid(String field, ConstraintValidatorContext constraintContext) {

            if (field.isEmpty()){
                LOGGER.debug("Field should not be empty");
                return false;
            }
            constraintContext.disableDefaultConstraintViolation();
            constraintContext.buildConstraintViolationWithTemplate(notNull.message()).addConstraintViolation();
            return true;
        }

    }
}

