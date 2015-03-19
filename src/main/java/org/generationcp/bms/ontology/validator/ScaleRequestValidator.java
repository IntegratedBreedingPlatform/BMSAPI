package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.ontology.dto.NameDescription;
import org.generationcp.bms.ontology.dto.ScaleRequest;
import org.generationcp.bms.ontology.dto.ValidValues;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.DataType;
import org.springframework.validation.Errors;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/** Add Scale
 * Validation rules for Scale request
 * Refer: http://confluence.leafnode.io/display/CD/Services+Validation
 1. Name is required
 2. The name must be unique
 3. Data type is required
 4. The data type ID must correspond to the ID of one of the supported data types (Numeric, Categorical, Character, DateTime, Person, Location or any other special data type that we add)
 5. If the data type is categorical, at least one category must be submitted
 6. Categories are only stored if the data type is categorical
 7. If there are categories, all labels and values within the set of categories must be unique
 8. The min and max valid values are only stored if the data type is numeric
 9. If the data type is numeric and minimum and maximum valid values are provided (they are not mandatory), they must be numeric values
 10. If present, the minimum valid value must be less than or equal to the maximum valid value, and the maximum valid value must be greater than or equal to the minimum valid value
 11. The name, data type and valid values cannot be changed if the scale is already in use
 */
@Component
public class ScaleRequestValidator extends BaseValidator implements org.springframework.validation.Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return ScaleRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        ScaleRequest request = (ScaleRequest) target;

        //1. Name is required
        shouldNotNullOrEmpty("name", request.getName(), errors);

        //2. The name must be unique
        checkTermUniqueness(request.getId(), request.getName(), CvId.SCALES.getId(), errors);

        //3. Data type is required
        shouldNotNullOrEmpty("dataTypeId", request.getDataTypeId(), errors);

        //4. The data type ID must correspond to the ID of one of the supported data types (Numeric, Categorical, Character, DateTime, Person, Location or any other special data type that we add)
        shouldHaveValidDataType("dataTypeId", request.getDataTypeId(), errors);

        //Need to return from here because other code is dependent on above validation
        if(errors.hasErrors()){
            return;
        }

        DataType dataType = DataType.getById(request.getDataTypeId());

        ValidValues validValues = Objects.isNull(request.getValidValues()) ? new ValidValues() : request.getValidValues();

        String minValue = validValues.getMinValue();
        String maxValue = validValues.getMaxValue();
        List<NameDescription> categories = validValues.getCategories();

        //5. If the data type is categorical, at least one category must be submitted
        if(Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE)){
            shouldNotNullOrEmpty("validValues.categories", categories, errors);
        }

        //TODO: Add more validation
        //6. Categories are only stored if the data type is categorical
        //7. If there are categories, all labels and values within the set of categories must be unique
        //8. The min and max valid values are only stored if the data type is numeric
        //9. If the data type is numeric and minimum and maximum valid values are provided (they are not mandatory), they must be numeric values
        //10. If present, the minimum valid value must be less than or equal to the maximum valid value, and the maximum valid value must be greater than or equal to the minimum valid value
        //11. The name, data type and valid values cannot be changed if the scale is already in use
    }
}
