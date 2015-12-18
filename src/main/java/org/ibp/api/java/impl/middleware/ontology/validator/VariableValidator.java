
package org.ibp.api.java.impl.middleware.ontology.validator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.google.common.base.Strings;

/**
 * Add/Update Variable Validation rules for Variable request Refer: http://confluence.leafnode.io/display/CD/Services+Validation 1. Name is
 * required 2. Name is no more than 32 characters 3. The name must be unique 4. Name must only contain alphanumeric characters, underscores
 * and cannot start with a number 5. Description is no more than 1024 characters 6. Property ID is required 7. Property ID must correspond
 * to the ID of an existing property 8. Method ID is required 9. Method ID must correspond to the ID of an existing method 10. Scale ID is
 * required 11. Scale ID must correspond to the ID of an existing scale 12. The combination of property, method and scale must not already
 * exist 13. The min and max expected range values are only stored if the scales data type is numeric 14. If the scale has a numeric data
 * type and a minimum and/or maximum value for the expected range is provided (it is not mandatory), the minimum and/or maximum must be
 * numeric values 15. If the scale has a numeric data type and valid values have * been set on the scale, the expected range minimum cannot
 * be less than the valid values minimum, and the expected range maximum cannot be larger than the valid values maximum 16. If provided, the
 * expected range minimum must be less than or equal to the expected range maximum, and the expected range maximum must be greater than or
 * equal to the expected range minimum 17. If present, Variable type IDs must be an array of integer values (or an empty array) 18. Variable
 * type IDs must be an array of integer values that correspond to the IDs of variable types and contain at least one item 19. Name,
 * property, method, scale, alias and expected range cannot be changed if the variable is already in use 20. Alias is no more than 32
 * characters 21. Alias must only contain alphanumeric characters, underscores and cannot start with a number 22. If variable with type
 * Analysis then we can not use other variable type with it.
 */

@Component
public class VariableValidator extends OntologyValidator implements Validator {

	private static final String VARIABLE_FIELD_SHOULD_HAVE_VALID_PATTERN = "variable.field.does.not.match.pattern";
	private static final String VARIABLE_WITH_SAME_COMBINATION_EXISTS = "variable.method.property.scale.combination.already.exist";
	private static final String VARIABLE_MIN_SHOULD_BE_IN_SCALE_RANGE = "variable.expected.min.should.not.be.smaller";
	private static final String VARIABLE_MAX_SHOULD_BE_IN_SCALE_RANGE = "variable.expected.max.should.not.be.greater";
	private static final String VARIABLE_SCALE_WITH_SYSTEM_DATA_TYPE = "variable.scale.system.datatype";
	private static final String VARIABLE_TYPE_ANALYSIS_SHOULD_BE_USED_SINGLE = "variable.type.analysis.can.not.club.with.other";

	private static final Integer NAME_TEXT_LIMIT = 32;
	private static final Integer DESCRIPTION_TEXT_LIMIT = 1024;

	private static final Logger LOGGER = LoggerFactory.getLogger(VariableValidator.class);

	private static final String PROPERTY_ID_NAME = "propertyId";
	private static final String METHOD_ID_NAME = "methodId";
	private static final String SCALE_ID_NAME = "scaleId";
	private static final String EXPECTED_RANGE_NAME = "expectedRange";
	private static final String VARIABLE_NAME = "variable";

	@Override
	public boolean supports(Class<?> aClass) {
		return VariableDetails.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		VariableDetails variable = (VariableDetails) target;

		boolean nameValidationResult = this.nameValidationProcessor(variable, errors);

		boolean descriptionValidationResult = this.descriptionValidationProcessor(variable, errors);

		boolean propertyValidationResult = this.propertyIdValidationProcessor(variable, errors);

		boolean methodValidationResult = this.methodIdValidationProcessor(variable, errors);

		boolean scaleValidationResult = this.scaleIdValidationProcessor(variable, errors);

		boolean scaleDataTypeValidationResult = false;

		if (scaleValidationResult) {
			scaleDataTypeValidationResult = this.scaleDataTypeValidationProcessor(variable, errors);
		}

		boolean combinationValidationResult = propertyValidationResult && methodValidationResult && scaleValidationResult;

		if (combinationValidationResult) {
			combinationValidationResult = this.checkIfMethodPropertyScaleCombination(variable, errors);
		}

		boolean variableTypeValidationResult = this.variableTypeValidationProcessor(variable, errors);

		if (nameValidationResult && descriptionValidationResult && combinationValidationResult && scaleDataTypeValidationResult
				&& variableTypeValidationResult) {

			// 19. Name, property, method, scale, alias and expected range cannot be changed if the variable is already in use
			this.variableShouldBeEditable(variable, errors);
		}
	}

	private boolean nameValidationProcessor(VariableDetails variable, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		// 1. Name is required
		this.shouldNotNullOrEmpty("Name", "name", variable.getName(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// Trim name
		variable.setName(variable.getName().trim());

		// 2. Name is no more than 32 characters
		this.fieldShouldNotOverflow("name", variable.getName(), VariableValidator.NAME_TEXT_LIMIT, errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 3. The name must be unique
		this.checkTermUniqueness("Variable", StringUtil.parseInt(variable.getId(), null), variable.getName(), CvId.VARIABLES.getId(),
				errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 4. Name must only contain alphanumeric characters, underscores and cannot start with a number
		this.fieldShouldHaveValidPattern("name", variable.getName(), "Name", errors);

		return errors.getErrorCount() == initialCount;
	}

	// 5. Description is optional and no more than 1024 characters
	private boolean descriptionValidationProcessor(VariableDetails variable, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		if (Strings.isNullOrEmpty(variable.getDescription())) {
			variable.setDescription("");
		} else {
			variable.setDescription(variable.getDescription().trim());
		}

		this.fieldShouldNotOverflow("description", variable.getDescription(), VariableValidator.DESCRIPTION_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean propertyIdValidationProcessor(VariableDetails variable, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		// 6. Property ID is required
		this.shouldNotNullOrEmpty("Property", VariableValidator.PROPERTY_ID_NAME, variable.getProperty().getId(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		Integer propertyId = StringUtil.parseInt(variable.getProperty().getId(), null);

		if (propertyId == null) {
			this.addCustomError(errors, VariableValidator.PROPERTY_ID_NAME, BaseValidator.FIELD_SHOULD_BE_NUMERIC, null);
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 7. Property ID must correspond to the ID of an existing property
		this.checkTermExist("Property", VariableValidator.PROPERTY_ID_NAME, variable.getProperty().getId(), CvId.PROPERTIES.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean methodIdValidationProcessor(VariableDetails variable, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		// 8. Method ID is required
		this.shouldNotNullOrEmpty("Method", VariableValidator.METHOD_ID_NAME, variable.getMethod().getId(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		Integer methodId = StringUtil.parseInt(variable.getMethod().getId(), null);

		if (methodId == null) {
			this.addCustomError(errors, VariableValidator.METHOD_ID_NAME, BaseValidator.FIELD_SHOULD_BE_NUMERIC, null);
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 9. Method ID must correspond to the ID of an existing method
		this.checkTermExist("Method", VariableValidator.METHOD_ID_NAME, variable.getMethod().getId(), CvId.METHODS.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean scaleIdValidationProcessor(VariableDetails variable, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		// 10. Scale ID is required
		this.shouldNotNullOrEmpty("Scale", VariableValidator.SCALE_ID_NAME, variable.getScale().getId(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		Integer scaleId = StringUtil.parseInt(variable.getScale().getId(), null);

		if (scaleId == null) {
			this.addCustomError(errors, VariableValidator.SCALE_ID_NAME, BaseValidator.FIELD_SHOULD_BE_NUMERIC, null);
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 11. Scale ID must correspond to the ID of an existing scale
		this.checkTermExist("Scale", VariableValidator.SCALE_ID_NAME, variable.getScale().getId(), CvId.SCALES.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean scaleDataTypeValidationProcessor(VariableDetails variable, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		// 13. The min and max expected range values are only stored if the scales data type is numeric
		try {
			Scale scale = this.ontologyScaleDataManager.getScaleById(StringUtil.parseInt(variable.getScale().getId(), null), true);

			if (scale.getDataType() != null && scale.getDataType().isSystemDataType()) {
				this.addCustomError(errors, VariableValidator.VARIABLE_SCALE_WITH_SYSTEM_DATA_TYPE, null);
			}

			if (errors.getErrorCount() > initialCount) {
				return false;
			}

			boolean isNumericType = Objects.equals(scale.getDataType(), DataType.NUMERIC_VARIABLE);

			if (isNumericType) {

				BigDecimal variableExpectedMin = null;
				BigDecimal variableExpectedMax = null;

				String variableMin = variable.getExpectedRange().getMin() == null ? null : variable.getExpectedRange().getMin();

				if (!this.isNullOrEmpty(variableMin)) {
					variableExpectedMin = StringUtil.parseBigDecimal(variableMin, null);
					if (variableExpectedMin == null) {
						this.addCustomError(errors, VariableValidator.EXPECTED_RANGE_NAME + ".min", BaseValidator.FIELD_SHOULD_BE_NUMERIC,
								null);
					}
				}

				String variableMax = variable.getExpectedRange().getMax() == null ? null : variable.getExpectedRange().getMax();

				if (!this.isNullOrEmpty(variableMax)) {
					variableExpectedMax = StringUtil.parseBigDecimal(variableMax, null);
					if (variableExpectedMax == null) {
						this.addCustomError(errors, VariableValidator.EXPECTED_RANGE_NAME + ".max", BaseValidator.FIELD_SHOULD_BE_NUMERIC,
								null);
					}
				}

				if (errors.getErrorCount() > initialCount) {
					return false;
				}

				// 14. If the scale has a numeric data type and valid values have been set on the scale, the expected range minimum cannot
				// be less than the valid values minimum, and the expected range maximum cannot be larger than the valid values maximum
				BigDecimal scaleMinValue = StringUtil.parseBigDecimal(scale.getMinValue(), null);
				if (scaleMinValue != null && variableExpectedMin != null && scaleMinValue.compareTo(variableExpectedMin) == 1) {
					this.addCustomError(errors, VariableValidator.EXPECTED_RANGE_NAME + ".min",
							VariableValidator.VARIABLE_MIN_SHOULD_BE_IN_SCALE_RANGE,
							new Object[] {scale.getMinValue(), scale.getMaxValue()});
				}

				BigDecimal scaleMaxValue = StringUtil.parseBigDecimal(scale.getMaxValue(), null);
				if (scaleMaxValue != null && variableExpectedMax != null && scaleMaxValue.compareTo(variableExpectedMax) == -1) {
					this.addCustomError(errors, VariableValidator.EXPECTED_RANGE_NAME + ".max",
							VariableValidator.VARIABLE_MAX_SHOULD_BE_IN_SCALE_RANGE,
							new Object[] {scale.getMinValue(), scale.getMaxValue()});
				}

				if (errors.getErrorCount() > initialCount) {
					return false;
				}

				// 15. If provided, the expected range minimum must be less than or equal to the expected range maximum, and the expected
				// range maximum must be greater than or equal to the expected range minimum
				if (variableExpectedMin != null && variableExpectedMax != null && variableExpectedMin.compareTo(variableExpectedMax) == 1) {
					this.addCustomError(errors, VariableValidator.EXPECTED_RANGE_NAME, BaseValidator.MIN_SHOULD_NOT_GREATER_THEN_MAX, null);
				}
			}

		} catch (MiddlewareException e) {
			VariableValidator.LOGGER.error("Error in validating VariableRequest", e);
		}

		return errors.getErrorCount() == initialCount;
	}

	private boolean variableTypeValidationProcessor(VariableDetails variable, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		// 17. Variable type IDs is required
		if (variable.getVariableTypes().isEmpty()) {
			this.addCustomError(errors, "variableTypes", BaseValidator.LIST_SHOULD_NOT_BE_EMPTY, new Object[] {"variable type"});
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 18. Variable type IDs must be an array of integer values that correspond to the IDs of variable types and contain at least one
		// item

		for (VariableType variableType : variable.getVariableTypes()) {
			if (org.generationcp.middleware.domain.ontology.VariableType.getById(this.parseVariableTypeAsInteger(variableType)) == null) {
				this.addCustomError(errors, "variableTypes", BaseValidator.INVALID_TYPE_ID, new Object[] {"Variable Type"});
			}
		}

        if(this.isAnalysisVariable(variable) && variable.getVariableTypes().size() > 1) {
				this.addCustomError(errors, "variableTypes", VariableValidator.VARIABLE_TYPE_ANALYSIS_SHOULD_BE_USED_SINGLE, new Object[]{"Variable Type"});
		}

		return errors.getErrorCount() == initialCount;
	}

	private void aliasValidationProcessor(VariableDetails variable, Errors errors) {

		if (!this.isNullOrEmpty(variable.getAlias())) {
			// Trim alias
			variable.setAlias(variable.getAlias().trim());

			// 20. Alias is no more than 32 characters
			this.fieldShouldNotOverflow("alias", variable.getAlias(), VariableValidator.NAME_TEXT_LIMIT, errors);

			// 21. Alias must only contain alphanumeric characters, underscores and cannot start with a number
			this.fieldShouldHaveValidPattern("alias", variable.getAlias(), "Alias", errors);
		}
	}

	void variableShouldBeEditable(VariableDetails variable, Errors errors) {

		if (variable.getId() == null) {
			return;
		}

		Integer initialCount = errors.getErrorCount();

		try {

			Integer requestId = StringUtil.parseInt(variable.getId(), null);
			Variable oldVariable = this.ontologyVariableDataManager.getVariable(variable.getProgramUuid(), requestId, true, true);

			if (oldVariable.getScale().getDataType() != null
					&& Objects.equals(oldVariable.getScale().getDataType().isSystemDataType(), true)) {
				this.addCustomError(errors, VariableValidator.VARIABLE_SCALE_WITH_SYSTEM_DATA_TYPE, null);
			}

			if (errors.getErrorCount() > initialCount) {
				return;
			}

			// that variable should exist with requestId
			if (Objects.equals(oldVariable, null)) {
				this.addCustomError(errors, BaseValidator.ID_DOES_NOT_EXIST,
						new Object[] {VariableValidator.VARIABLE_NAME, variable.getId()});
				return;
			}

			if (oldVariable.getObservations() == null) {
				oldVariable.setObservations(0);
			}

			boolean isEditable = oldVariable.getObservations() == 0;
			if (isEditable) {
				// Alias validation
				this.aliasValidationProcessor(variable, errors);
				return;
			}

			Integer methodId = StringUtil.parseInt(variable.getMethod().getId(), null);
			Integer propertyId = StringUtil.parseInt(variable.getProperty().getId(), null);
			Integer scaleId = StringUtil.parseInt(variable.getScale().getId(), null);

			boolean nameEqual = StringUtil.areBothEmptyOrEqual(variable.getName(), oldVariable.getName());
			boolean propertyEqual = Objects.equals(propertyId, oldVariable.getProperty().getId());
			boolean methodEqual = Objects.equals(methodId, oldVariable.getMethod().getId());
			boolean scaleEqual = Objects.equals(scaleId, oldVariable.getScale().getId());
			boolean minValuesEqual = StringUtil.areBothEmptyOrEqual(variable.getExpectedRange().getMin(), oldVariable.getMinValue());
			boolean maxValuesEqual = StringUtil.areBothEmptyOrEqual(variable.getExpectedRange().getMax(), oldVariable.getMaxValue());
			boolean aliasEqual = StringUtil.areBothEmptyOrEqual(variable.getAlias(), oldVariable.getAlias());

			if (!nameEqual) {
				this.addCustomError(errors, "name", BaseValidator.RECORD_IS_NOT_EDITABLE, new Object[] {VariableValidator.VARIABLE_NAME,
				"name"});
				return;
			}

			if (!aliasEqual) {
				this.addCustomError(errors, "alias", BaseValidator.RECORD_IS_NOT_EDITABLE, new Object[] {VariableValidator.VARIABLE_NAME,
						"alias"});
				return;
			}

			if (!propertyEqual) {
				this.addCustomError(errors, "propertyId", BaseValidator.RECORD_IS_NOT_EDITABLE, new Object[] {
						VariableValidator.VARIABLE_NAME, "property"});
				return;
			}

			if (!methodEqual) {
				this.addCustomError(errors, "methodId", BaseValidator.RECORD_IS_NOT_EDITABLE, new Object[] {
						VariableValidator.VARIABLE_NAME, "method"});
				return;
			}

			if (!scaleEqual) {
				this.addCustomError(errors, "scaleId", BaseValidator.RECORD_IS_NOT_EDITABLE, new Object[] {VariableValidator.VARIABLE_NAME,
				"scale"});
				return;
			}

			if (!minValuesEqual || !maxValuesEqual) {
				this.addCustomError(errors, "expectedRange", BaseValidator.RECORD_IS_NOT_EDITABLE, new Object[] {
						VariableValidator.VARIABLE_NAME, "expectedRange"});
			}

		} catch (Exception e) {
			VariableValidator.LOGGER.error("Error while executing variableShouldBeEditable", e);
			this.addDefaultError(errors);
		}
	}

	protected void fieldShouldHaveValidPattern(String fieldName, String value, String termName, Errors errors) {
		Pattern regex = Pattern.compile("[$&+,./%')\\[}\\]{(*^!`~:;=?@#|\\s]");
		Matcher matcher = regex.matcher(value);

		if (matcher.find() || Character.isDigit(value.charAt(0))) {
			this.addCustomError(errors, fieldName, VariableValidator.VARIABLE_FIELD_SHOULD_HAVE_VALID_PATTERN, new Object[] {termName});
		}
	}

	// FIXME : Spring Does not allow multiple fields in rejectValue so here fieldNames have been not added
	protected boolean checkIfMethodPropertyScaleCombination(VariableDetails variable, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		try {

			Integer methodId = StringUtil.parseInt(variable.getMethod().getId(), null);
			Integer propertyId = StringUtil.parseInt(variable.getProperty().getId(), null);
			Integer scaleId = StringUtil.parseInt(variable.getScale().getId(), null);

			VariableFilter variableFilter = new VariableFilter();
			variableFilter.addMethodId(methodId);
			variableFilter.addPropertyId(propertyId);
			variableFilter.addScaleId(scaleId);

			List<Variable> variableSummary = this.ontologyVariableDataManager.getWithFilter(variableFilter);

			if (variableSummary.size() > 1 || variableSummary.size() == 1
					&& !Objects.equals(String.valueOf(variableSummary.get(0).getId()), variable.getId())) {
				this.addCustomError(errors, VariableValidator.VARIABLE_WITH_SAME_COMBINATION_EXISTS, null);
			}
		} catch (MiddlewareException e) {
			VariableValidator.LOGGER.error("Error occur while fetching variable in checkIfMethodPropertyScaleCombination", e);
		}

		return errors.getErrorCount() == initialCount;
	}

	private Integer parseVariableTypeAsInteger(VariableType variableType) {
		if (variableType == null) {
			return null;
		}
		return StringUtil.parseInt(variableType.getId(), null);
	}

	private boolean isAnalysisVariable(VariableDetails variable) {
        for(org.ibp.api.domain.ontology.VariableType type : variable.getVariableTypes()){
            if(Objects.equals(StringUtil.parseInt(type.getId(), 0), org.generationcp.middleware.domain.ontology.VariableType.ANALYSIS.getId())){
                return true;
            }
        }
        return false;
    }

}
