package org.generationcp.bms.ontology.validator;

import com.google.common.base.Strings;
import org.generationcp.bms.ontology.dto.NameDescription;
import org.generationcp.bms.ontology.dto.ScaleRequest;
import org.generationcp.bms.ontology.dto.ValidValues;
import org.generationcp.bms.util.I18nUtil;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.DataType;
import org.springframework.validation.Errors;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


import org.springframework.stereotype.Component;


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
public class ScaleRequestValidator extends OntologyValidator implements org.springframework.validation.Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return ScaleRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        final String CATEGORIES_SHOULD_BE_EMPTY_FOR_NON_CATEGORICAL_DATA_TYPE = "scale.categories.should.not.pass.with.non.categorical.data.type";
        final String CATEGORIES_NAME_DUPLICATE = "scale.categories.name.duplicate";
        final String CATEGORIES_DESCRIPTION_DUPLICATE = "scale.categories.description.duplicate";
        final String MIN_MAX_NOT_EXPECTED = "scale.min.max.should.not.supply.when.data.type.non.numeric";
        final String MIN_MAX_NOT_VALID = "scale.min.max.not.valid";
        final String VALUE_SHOULD_BE_NUMERIC = "value.should.be.numeric";

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

        ValidValues validValues = request.getValidValues() == null ? new ValidValues() : request.getValidValues();

        String minValue = validValues.getMinValue();
        String maxValue = validValues.getMaxValue();
        List<NameDescription> categories = validValues.getCategories();

        //5. If the data type is categorical, at least one category must be submitted
        if(Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE)){
            shouldNotNullOrEmpty("validValues.categories", categories, errors);
        }

        //6. Categories are only stored if the data type is categorical
        if(!Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE) && !isNullOrEmpty(categories)){
            errors.rejectValue("validValues.categories", I18nUtil.formatErrorMessage(messageSource, CATEGORIES_SHOULD_BE_EMPTY_FOR_NON_CATEGORICAL_DATA_TYPE, null));
        }

        //7. If there are categories, all labels and values within the set of categories must be unique
        if(Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE) && categories != null){
            Set<String> labels = new HashSet<>();
            Set<String> values = new HashSet<>();
            for(int i = 0; i < categories.size(); i++){
                NameDescription nameDescription = categories.get(i);
                String name = nameDescription.getName();
                String value = nameDescription.getDescription();

                if(Strings.isNullOrEmpty(name) || labels.contains(nameDescription.getName())){
                    errors.rejectValue("validValues.categories[" + i + "].name", I18nUtil.formatErrorMessage(messageSource, CATEGORIES_NAME_DUPLICATE, null));
                } else{
                    labels.add(nameDescription.getName());
                }

                if(Strings.isNullOrEmpty(value) || values.contains(value)){
                    errors.rejectValue("validValues.categories[" + i + "].description", I18nUtil.formatErrorMessage(messageSource, CATEGORIES_DESCRIPTION_DUPLICATE, null));
                } else{
                    values.add(nameDescription.getDescription());
                }
            }
        }

        //8. The min and max valid values are only stored if the data type is numeric
        if((!Objects.equals(dataType, DataType.NUMERIC_VARIABLE)) && (minValue != null || maxValue != null)){
            errors.rejectValue("validValues", I18nUtil.formatErrorMessage(messageSource, MIN_MAX_NOT_EXPECTED, null));
        }

        //9. If the data type is numeric and minimum and maximum valid values are provided (they are not mandatory), they must be numeric values
        if(Objects.equals(dataType, DataType.NUMERIC_VARIABLE)){
            if(minValue != null && !isNonNullValidNumericString(minValue)){
                errors.rejectValue("validValues.minValue", I18nUtil.formatErrorMessage(messageSource, VALUE_SHOULD_BE_NUMERIC, null));
            }

            if(maxValue != null && !isNonNullValidNumericString(maxValue)){
                errors.rejectValue("validValues.maxValue", I18nUtil.formatErrorMessage(messageSource, VALUE_SHOULD_BE_NUMERIC, null));
            }
        }

        //10. If present, the minimum valid value must be less than or equal to the maximum valid value, and the maximum valid value must be greater than or equal to the minimum valid value
        if(isNonNullValidNumericString(minValue) && isNonNullValidNumericString(maxValue)){
            if(getIntegerValueSafe(minValue, 0) > getIntegerValueSafe(maxValue, 0)){
                errors.rejectValue("validValues", I18nUtil.formatErrorMessage(messageSource, MIN_MAX_NOT_VALID, null));
            }
        }

        //TODO: Add more validation
        //11. The name, data type and valid values cannot be changed if the scale is already in use
    }
}
