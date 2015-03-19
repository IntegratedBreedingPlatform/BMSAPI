package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.ScaleRequest;
import org.generationcp.middleware.domain.oms.CvId;
import org.springframework.validation.Errors;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ScaleRequestValidator extends BaseValidator implements org.springframework.validation.Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return ScaleRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        ScaleRequest request = (ScaleRequest) target;

        shouldNotNullOrEmpty("name", request.getName(), errors);
        shouldNotNullOrEmpty("dataTypeId", request.getDataTypeId(), errors);

        if(Objects.nonNull(request.getValidValues())){
            shouldNotNullOrEmpty("validValues.categories", request.getValidValues().getCategories(), errors);
        }

        checkTermUniqueness(request.getId(), request.getName(), CvId.SCALES.getId(), errors);

        //TODO: Add more validation
    }
}
