package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Strings;
import org.generationcp.middleware.domain.oms.*;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.CommonUtil;
import org.ibp.api.domain.ontology.ExpectedRange;
import org.ibp.api.domain.ontology.VariableRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Add/Update Variable Validation rules for Variable request Refer:
 * http://confluence.leafnode.io/display/CD/Services+Validation
 * 1. Name is required
 * 2. Name is no more than 32 characters
 * 3. The name must be unique
 * 4. Name must only contain alphanumeric characters, underscores and cannot start with a number
 * 5. Description is no more than 255 characters
 * 6. Property ID is required
 * 7. Property ID must correspond to the ID of an existing property
 * 8. Method ID is required
 * 9. Method ID must correspond to the ID of an existing method
 * 10. Scale ID is required
 * 11. Scale ID must correspond to the ID of an existing scale
 * 12. The combination of property, method and scale must not already exist
 * 13. The min and max expected range values are only stored if the scales data type is numeric
 * 14. If the scale has a numeric data type and a minimum and/or maximum value for the expected range is provided (it is not mandatory), the minimum and/or maximum must be numeric values
 * 15. If the scale has a numeric data type and valid values have * been set on the scale, the expected range minimum cannot be less than the valid values minimum, and the expected range maximum cannot be larger than the valid values maximum
 * 16. If provided, the expected range minimum must be less than or equal to the expected range maximum, and the expected range maximum must be greater than or equal to the expected range minimum
 * 17. If present, Variable type IDs must be an array of integer values (or an empty array)
 * 18. Variable type IDs must be an array of integer values that correspond to the IDs of variable types and contain at least one item
 */

@Component
public class VariableRequestValidator extends OntologyValidator implements Validator {

	private static final String VARIABLE_NAME_SHOULD_HAVE_VALID_PATTERN = "variable.name.does.not.match.pattern";
	private static final String VARIABLE_WITH_SAME_COMBINATION_EXISTS = "variable.method.property.scale.combination.already.exist";
	private static final String VARIABLE_MIN_SHOULD_BE_IN_SCALE_RANGE = "variable.expected.min.should.not.be.smaller";
	private static final String VARIABLE_MAX_SHOULD_BE_IN_SCALE_RANGE = "variable.expected.max.should.not.be.greater";

	private static final Integer NAME_TEXT_LIMIT = 32;
	private static final Integer DESCRIPTION_TEXT_LIMIT = 255;

	@Override
	public boolean supports(Class<?> aClass) {
		return VariableRequest.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		VariableRequest request = (VariableRequest) target;

		boolean nameValidationResult = nameValidationProcessor(request, errors);

		boolean descriptionValidationResult = descriptionValidationProcessor(request, errors);

		boolean propertyValidationResult = propertyIdValidationProcessor(request, errors);

		boolean methodValidationResult = methodIdValidationProcessor(request, errors);

		boolean scaleValidationResult = scaleIdValidationProcessor(request, errors);

		boolean scaleDataTypeValidationResult = false;

		if(scaleValidationResult) {
			scaleDataTypeValidationResult = scaleDataTypeValidationProcessor(request, errors);
		}

		boolean combinationValidationResult = propertyValidationResult && methodValidationResult && scaleValidationResult;

		if(combinationValidationResult){

			combinationValidationResult = checkIfMethodPropertyScaleCombination(request, errors);
		}

		boolean variableTypeValidationResult = variableTypeValidationProcessor(request, errors);

		if(nameValidationResult && descriptionValidationResult && combinationValidationResult && scaleDataTypeValidationResult && variableTypeValidationResult){

			// 19. Name, property, method, scale, alias and expected range cannot be changed if the variable is already in use
			this.variableShouldBeEditable(request, errors);
		}
	}


	private boolean nameValidationProcessor(VariableRequest request, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 1. Name is required
		this.shouldNotNullOrEmpty("Name", "name", request.getName(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		//Trim name
		request.setName(request.getName().trim());

		// 2. Name is no more than 32 characters
		this.fieldShouldNotOverflow("name", request.getName(), NAME_TEXT_LIMIT,errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 3. The name must be unique
		this.checkTermUniqueness("Variable", CommonUtil.tryParseSafe(request.getId()), request.getName(), CvId.VARIABLES.getId(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		//4. Name must only contain alphanumeric characters, underscores and cannot start with a number
		this.nameShouldHaveValidPattern("name", request.getName(), errors);

		return errors.getErrorCount() == initialCount;
	}

	// 5. Description is optional and no more than 255 characters
	private boolean descriptionValidationProcessor(VariableRequest request, Errors errors){

		Integer initialCount = errors.getErrorCount();

		if(Strings.isNullOrEmpty(request.getDescription())) {
			request.setDescription("");
		} else {
			request.setDescription(request.getDescription().trim());
		}

		this.fieldShouldNotOverflow("description", request.getDescription(), DESCRIPTION_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean propertyIdValidationProcessor(VariableRequest request, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 6. Property ID is required
		this.shouldNotNullOrEmpty("Property", "propertyId", request.getPropertyId(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 7. Property ID must correspond to the ID of an existing property
		this.checkTermExist("Property", "propertyId", request.getPropertyId(), CvId.PROPERTIES.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean methodIdValidationProcessor(VariableRequest request, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 8. Method ID is required
		this.shouldNotNullOrEmpty("Method", "methodId", request.getMethodId(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 9. Method ID must correspond to the ID of an existing method
		this.checkTermExist("Method", "methodId", request.getMethodId(), CvId.METHODS.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean scaleIdValidationProcessor(VariableRequest request, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 10. Scale ID is required
		this.shouldNotNullOrEmpty("Scale", "scaleId", request.getScaleId(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 11. Scale ID must correspond to the ID of an existing scale
		this.checkTermExist("Scale", "scaleId", request.getScaleId(), CvId.SCALES.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean scaleDataTypeValidationProcessor(VariableRequest request, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 13. The min and max expected range values are only stored if the scales data type is numeric
		try {
			Scale scale = this.ontologyManagerService.getScaleById(CommonUtil.tryParseSafe(request.getScaleId()));

			boolean isNumericType = Objects.equals(scale.getDataType(), DataType.NUMERIC_VARIABLE);

			if(request.getExpectedRange() == null) {
				request.setExpectedRange(new ExpectedRange());
			}

			String minValue = request.getExpectedRange().getMin();
			String maxValue = request.getExpectedRange().getMax();


			if (isNumericType) {
				// 14. If the scale has a numeric data type and a minimum and/or maximum value for the expected range is provided (it is not mandatory), the minimum and/or maximum must be numeric values
				if (!this.isNonNullValidNumericString(minValue)) {
					this.addCustomError(errors, "expectedRange.min", FIELD_SHOULD_BE_NUMERIC, null);
				}

				if (!this.isNonNullValidNumericString(maxValue)) {
					this.addCustomError(errors, "expectedRange.max", FIELD_SHOULD_BE_NUMERIC, null);
				}

				if (errors.getErrorCount() > initialCount) {
					return false;
				}

				// 14. If the scale has a numeric data type and valid values have been set on the scale, the expected range minimum cannot be less than the valid values minimum, and the expected range maximum cannot be larger than the valid values maximum
				if (this.getIntegerValueSafe(minValue, 0) < this.getIntegerValueSafe(scale.getMinValue(), 0)) {
					this.addCustomError(errors, "expectedRange.min", VARIABLE_MIN_SHOULD_BE_IN_SCALE_RANGE, new Object[] { scale.getMinValue(), scale.getMaxValue() });
				}
				if (this.getIntegerValueSafe(request.getExpectedRange().getMax(), 0) > this.getIntegerValueSafe(scale.getMaxValue(), 0)) {
					this.addCustomError(errors, "expectedRange.max", VARIABLE_MAX_SHOULD_BE_IN_SCALE_RANGE, new Object[] { scale.getMinValue(), scale.getMaxValue() });
				}

				if (errors.getErrorCount() > initialCount) {
					return false;
				}

				// 15. If provided, the expected range minimum must be less than or equal to the expected range maximum, and the expected range maximum must be greater than or equal to the expected range minimum
				if (this.getIntegerValueSafe(minValue, 0) > this.getIntegerValueSafe(maxValue, 0)) {
					this.addCustomError(errors, "expectedRange", MIN_SHOULD_NOT_GREATER_THEN_MAX, null);
				}
			}

		} catch (MiddlewareException e) {
			e.printStackTrace();
		}

		return errors.getErrorCount() == initialCount;
	}

	private boolean variableTypeValidationProcessor(VariableRequest request, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 17. Variable type IDs is required
		if(request.getVariableTypeIds().isEmpty()){
			this.addCustomError(errors, "variableTypeIds", LIST_SHOULD_NOT_BE_EMPTY, new Object[]{"variable type"});
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 18. Variable type IDs must be an array of integer values that correspond to the IDs of variable types and contain at least one item
		for (String i : request.getVariableTypeIds()) {
			if (VariableType.getById(CommonUtil.tryParseSafe(i)) == null) {
				this.addCustomError(errors, "variableTypeIds", INVALID_TYPE_ID, new Object[] {"Variable Type"});
			}
		}

		return errors.getErrorCount() == initialCount;
	}

	private void variableShouldBeEditable(VariableRequest request, Errors errors) {

		if (request.getId() == null) {
			return;
		}

		try {

			Integer requestId = CommonUtil.tryParseSafe(request.getId());
			OntologyVariable oldVariable = this.ontologyManagerService.getVariable(request.getProgramUuid(), requestId);

			// that variable should exist with requestId
			if (Objects.equals(oldVariable, null)) {
				this.addCustomError(errors, ID_DOES_NOT_EXIST, new Object[] { "Variable", request.getId() });
				return;
			}

			if(oldVariable.getObservations() == null) {
				oldVariable.setObservations(0);
			}

			boolean isEditable = oldVariable.getObservations() == 0;
			if (isEditable) {
				return;
			}

			Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
			Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
			Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

			boolean nameEqual = Objects.equals(request.getName(), oldVariable.getName());
			boolean propertyEqual = Objects.equals(propertyId, oldVariable.getProperty().getId());
			boolean methodEqual = Objects.equals(methodId, oldVariable.getMethod().getId());
			boolean scaleEqual = Objects.equals(scaleId, oldVariable.getScale().getId());
			boolean minValuesEqual = Objects.equals(request.getExpectedRange().getMin(), oldVariable.getMinValue());
			boolean maxValuesEqual = Objects.equals(request.getExpectedRange().getMax(), oldVariable.getMaxValue());

			if(!nameEqual){
				this.addCustomError(errors, "name", RECORD_IS_NOT_EDITABLE,new Object[] { "variable", "name" });
				return;
			}

			if(!propertyEqual){
				this.addCustomError(errors, "propertyId", RECORD_IS_NOT_EDITABLE,new Object[] { "variable", "property" });
				return;
			}

			if(!methodEqual){
				this.addCustomError(errors, "methodId", RECORD_IS_NOT_EDITABLE,new Object[] { "variable", "method" });
				return;
			}

			if(!scaleEqual){
				this.addCustomError(errors, "scaleId", RECORD_IS_NOT_EDITABLE,new Object[] { "variable", "scale" });
				return;
			}

			if(!minValuesEqual || !maxValuesEqual){
				this.addCustomError(errors, "expectedRange", RECORD_IS_NOT_EDITABLE,new Object[] { "variable", "expectedRange" });
			}

		} catch (Exception e) {
			this.log.error("Error while executing variableShouldBeEditable", e);
			this.addDefaultError(errors);
		}
	}

	protected void nameShouldHaveValidPattern(String fieldName, String value, Errors errors) {
		Pattern regex = Pattern.compile("[$&+,./%')\\[}\\]{(*^!`~:;=?@#|]");
		Matcher matcher = regex.matcher(value);

		if (matcher.find() || Character.isDigit(value.charAt(0))) {
			this.addCustomError(errors, fieldName, VARIABLE_NAME_SHOULD_HAVE_VALID_PATTERN, null);
		}
	}

	//FIXME : Spring Does not allow multiple fields in rejectValue so here fieldNames have been not added
	protected boolean checkIfMethodPropertyScaleCombination(VariableRequest request, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		try {

			Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
			Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
			Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

			List<OntologyVariableSummary> variableSummary = this.ontologyManagerService.getWithFilter(null, null, methodId, propertyId, scaleId);

			if(variableSummary.size() > 1 ||
					(variableSummary.size() == 1 && !Objects.equals(String.valueOf(variableSummary.get(0).getId()), request.getId()))) {
				this.addCustomError(errors, VARIABLE_WITH_SAME_COMBINATION_EXISTS, null);
			}
		} catch (MiddlewareException e) {
			this.log.error("Error occur while fetching variable in checkIfMethodPropertyScaleCombination", e);
		}

		return errors.getErrorCount() == initialCount;
	}
}
