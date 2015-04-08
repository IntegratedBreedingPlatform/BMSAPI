package org.ibp.api.java.impl.middleware.ontology.validator;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Scale;
import org.ibp.api.domain.ontology.VariableRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/** Add Variable
 * Validation rules for Variable request
 * Refer: http://confluence.leafnode.io/display/CD/Services+Validation
 1. Name is required
 2. Name is no more than 32 characters
 3. Description is no more than 255 characters
 4. The name must be unique
 5. Name must only contain alphanumeric characters, underscores and cannot start with a number
 6. Property ID is required
 7. Property ID must correspond to the ID of an existing property
 8. Method ID is required
 9. Method ID must correspond to the ID of an existing method
 10. Scale ID is required
 11. Scale ID must correspond to the ID of an existing scale
 12. The min and max expected range values are only stored if the scales data type is numeric
 13. If the scale has a numeric data type and a minimum and/or maximum value for the expected range is provided (it is not mandatory), the minimum and/or maximum must be numeric values
 14. If the scale has a numeric data type and valid values have been set on the scale, the expected range minimum cannot be less than the valid values minimum, and the expected range maximum cannot be larger than the valid values maximum
 15. If provided, the expected range minimum must be less than or equal to the expected range maximum, and the expected range maximum must be greater than or equal to the expected range minimum
 16. The combination of property, method and scale must not already exist
 17. If present, Variable type IDs must be an array of integer values (or an empty array)
 */

@Component
public class VariableRequestValidator extends OntologyValidator implements Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return VariableRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        VariableRequest request = (VariableRequest) target;

        //1. Name is required
        shouldNotNullOrEmpty("name", request.getName(), errors);

        //2. Name is no more than 32 characters
        nameShouldHaveMax32Chars("name", request.getName(), errors);

        //3. Description is no more than 255 characters
        descriptionShouldHaveMax255Chars("description", request.getDescription(), errors);

        //4. The name must be unique
        checkTermUniqueness(null, request.getName(), CvId.VARIABLES.getId(), errors);

        if(errors.hasErrors()){
            return;
        }

        //5. Name must only contain alphanumeric characters, underscores and cannot start with a number
        nameShouldNotHaveSpecialCharacterAndNoDigitInStart("name", request.getName(), errors);

        //6. Property ID is required
        checkIntegerNull("propertyId", request.getPropertyId(), errors);

        if(errors.hasErrors()){
            return;
        }

        //7. Property ID must correspond to the ID of an existing property
        checkTermExist("property", request.getPropertyId(), CvId.PROPERTIES.getId(), errors);

        //8. Method ID is required
        checkIntegerNull("methodId", request.getMethodId(), errors);

        if(errors.hasErrors()){
            return;
        }

        //9. Method ID must correspond to the ID of an existing method
        checkTermExist("method", request.getMethodId(), CvId.METHODS.getId(), errors);

        //10. Scale ID is required
        checkIntegerNull("scaleId", request.getScaleId(), errors);

        if(errors.hasErrors()){
            return;
        }

        //11. Scale ID must correspond to the ID of an existing scale
        checkTermExist("scale", request.getScaleId(), CvId.SCALES.getId(), errors);

        if(errors.hasErrors()){
            return;
        }

        //12. The min and max expected range values are only stored if the scales data type is numeric
        Scale scale = getScaleData(request.getScaleId());

        checkDataTypeNull("scaleId", scale, errors);

        if(errors.hasErrors()){
            return;
        }

        boolean isNumericType = checkNumericDataType(scale.getDataType());
        if(!isNumericType && !request.getExpectedRange().getMin().isEmpty() && !request.getExpectedRange().getMax().isEmpty()){
            addCustomError(errors, "expectedRange", MIN_MAX_NOT_EXPECTED, null);
        }

        //13. If the scale has a numeric data type and a minimum and/or maximum value for the expected range is provided (it is not mandatory), the minimum and/or maximum must be numeric values
        if(isNumericType){
            if(!isNonNullValidNumericString(request.getExpectedRange().getMin()) || !isNonNullValidNumericString(request.getExpectedRange().getMax())){
                addCustomError(errors, "expectedRange", SHOULD_NOT_NULL_OR_EMPTY, null);
                return;
            }
            if(request.getExpectedRange().getMin() != null && !isNonNullValidNumericString(request.getExpectedRange().getMin())){
                addCustomError(errors, "expectedRange", VALUE_SHOULD_BE_NUMERIC, null);
            }

            if(request.getExpectedRange().getMax() != null && !isNonNullValidNumericString(request.getExpectedRange().getMax())){
                addCustomError(errors, "expectedRange", VALUE_SHOULD_BE_NUMERIC, null);
            }
        }

        //14. If the scale has a numeric data type and valid values have been set on the scale, the expected range minimum cannot be less than the valid values minimum, and the expected range maximum cannot be larger than the valid values maximum
        if(isNumericType){
            if(getIntegerValueSafe(request.getExpectedRange().getMin(), 0) < getIntegerValueSafe(scale.getMinValue(), 0)){
                addCustomError(errors, "expectedRange", EXPECTED_MIN_SHOULD_NOT_LESSER_THAN_SCALE_MIN, null);
                return;
            }
            if(getIntegerValueSafe(request.getExpectedRange().getMax(), 0) > getIntegerValueSafe(scale.getMaxValue(), 0)){
                addCustomError(errors, "expectedRange", EXPECTED_MAX_SHOULD_NOT_GREATER_THAN_SCALE_MAX, null);
                return;
            }
        }

        //15. If provided, the expected range minimum must be less than or equal to the expected range maximum, and the expected range maximum must be greater than or equal to the expected range minimum
        if(getIntegerValueSafe(request.getExpectedRange().getMin(), 0) > getIntegerValueSafe(request.getExpectedRange().getMax(), 0)){
            addCustomError(errors,"expectedRange", MIN_MAX_NOT_VALID,null);
        }

        //16. The combination of property, method and scale must not already exist
        checkIfMethodPropertyScaleCombination("methodId", request.getMethodId(), request.getPropertyId(), request.getScaleId(), errors);

        //17. If present, Variable type IDs must be an array of integer values (or an empty array)
        shouldNotNullOrEmpty("variableTypeIds", request.getVariableTypeIds(), errors);

        if(errors.hasErrors()){
            return;
        }

        for(Integer i : request.getVariableTypeIds()){
            shouldHaveValidVariableType("variableTypeIds", i, errors);
        }
    }
}
