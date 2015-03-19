package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.ScaleRequest;
import org.generationcp.middleware.domain.oms.CvId;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.generationcp.bms.util.I18nUtil.formatErrorMessage;

import org.springframework.stereotype.Component;

@Component
public class ScaleRequestValidator extends BaseValidator implements org.springframework.validation.Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return ScaleRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        ScaleRequest request = (ScaleRequest) target;

        if(isNullOrEmpty(request.getName())){
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", formatErrorMessage(messageSource, "should.not.be.null", null));
        }
        if(Objects.isNull(request.getDataTypeId())){
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "dataTypeId", formatErrorMessage(messageSource, "data.type.should.not.be.null", null));
        }

        checkUniqueness(request.getId(), request.getName(), CvId.SCALES.getId(), errors);

        //TODO: Add more validation
    }
}
