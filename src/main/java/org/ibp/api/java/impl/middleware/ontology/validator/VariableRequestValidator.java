package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Strings;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.OntologyVariable;
import org.generationcp.middleware.domain.oms.Scale;
import org.ibp.api.domain.ontology.ExpectedRange;
import org.ibp.api.domain.ontology.VariableRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

/**
 * Add Variable Validation rules for Variable request Refer:
 * http://confluence.leafnode.io/display/CD/Services+Validation 1. Name is
 * required 2. Name is no more than 32 characters 3. Description is no more than
 * 255 characters 4. The name must be unique 5. Name must only contain
 * alphanumeric characters, underscores and cannot start with a number 6.
 * Property ID is required 7. Property ID must correspond to the ID of an
 * existing property 8. Method ID is required 9. Method ID must correspond to
 * the ID of an existing method 10. Scale ID is required 11. Scale ID must
 * correspond to the ID of an existing scale 12. The min and max expected range
 * values are only stored if the scales data type is numeric 13. If the scale
 * has a numeric data type and a minimum and/or maximum value for the expected
 * range is provided (it is not mandatory), the minimum and/or maximum must be
 * numeric values 14. If the scale has a numeric data type and valid values have
 * been set on the scale, the expected range minimum cannot be less than the
 * valid values minimum, and the expected range maximum cannot be larger than
 * the valid values maximum 15. If provided, the expected range minimum must be
 * less than or equal to the expected range maximum, and the expected range
 * maximum must be greater than or equal to the expected range minimum 16. The
 * combination of property, method and scale must not already exist 17. If
 * present, Variable type IDs must be an array of integer values (or an empty
 * array) 18. Variable type IDs must be an array of integer values that correspond
 * to the IDs of variable types and contain at least one item
 */

@Component
public class VariableRequestValidator extends OntologyValidator implements Validator {

	@Override
	public boolean supports(Class<?> aClass) {
		return VariableRequest.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		VariableRequest request = (VariableRequest) target;

		// 19. Name, property, method, scale, alias and expected range cannot be changed if the variable is already in use
		this.variableShouldBeEditable(request, errors);

		// 1. Name is required
		this.shouldNotNullOrEmpty("name", request.getName(), errors);

		// 2. Name is no more than 32 characters
		this.nameShouldHaveMax32Chars("name", request.getName(), errors);

		// 3. Description is no more than 255 characters
		this.descriptionShouldHaveMax255Chars("description", request.getDescription(), errors);

		// 4. The name must be unique
		this.checkTermUniqueness(null, request.getName(), CvId.VARIABLES.getId(), "variable", errors);

		if (errors.hasErrors()) {
			return;
		}

		// 5. Name must only contain alphanumeric characters, underscores and
		// cannot start with a number
		this.nameShouldNotHaveSpecialCharacterAndNoDigitInStart("name", request.getName(), errors);

		// 6. Property ID is required
		this.checkIntegerNull("propertyId", request.getPropertyId(), errors);

		if (errors.hasErrors()) {
			return;
		}

		String propertyId = String.valueOf(request.getPropertyId());
		String methodId = String.valueOf(request.getMethodId());
		String scaleId = String.valueOf(request.getScaleId());

		// 7. Property ID must correspond to the ID of an existing property
		this.checkTermExist("property", propertyId, CvId.PROPERTIES.getId(), errors);

		// 8. Method ID is required
		this.checkIntegerNull("methodId", request.getMethodId(), errors);

		if (errors.hasErrors()) {
			return;
		}

		// 9. Method ID must correspond to the ID of an existing method
		this.checkTermExist("method", methodId, CvId.METHODS.getId(), errors);

		// 10. Scale ID is required
		this.checkIntegerNull("scaleId", request.getScaleId(), errors);

		if (errors.hasErrors()) {
			return;
		}

		// 11. Scale ID must correspond to the ID of an existing scale
		this.checkTermExist("scale", scaleId, CvId.SCALES.getId(), errors);

		if (errors.hasErrors()) {
			return;
		}

		// 12. The min and max expected range values are only stored if the scales data type is numeric
		Scale scale = this.getScaleData(request.getScaleId());

		boolean isNumericType = this.checkNumericDataType(scale.getDataType());

		if(request.getExpectedRange() == null) {
			request.setExpectedRange(new ExpectedRange());
		}

		String minValue = request.getExpectedRange().getMin();
		String maxValue = request.getExpectedRange().getMax();

		if (!isNumericType && (!Strings.isNullOrEmpty(minValue) || !Strings.isNullOrEmpty(maxValue))) {
			this.addCustomError(errors, "expectedRange", OntologyValidator.MIN_MAX_NOT_EXPECTED, null);
		}

		// 13. If the scale has a numeric data type and a minimum and/or maximum
		// value for the expected range is provided (it is not mandatory), the
		// minimum and/or maximum must be numeric values
		if (isNumericType) {
			if (!this.isNonNullValidNumericString(minValue)) {
				this.addCustomError(errors, "expectedRange", OntologyValidator.VALUE_SHOULD_BE_NUMERIC, null);
			}

			if (!this.isNonNullValidNumericString(maxValue)) {
				this.addCustomError(errors, "expectedRange", OntologyValidator.VALUE_SHOULD_BE_NUMERIC, null);
			}
		}

		if(errors.hasErrors()) {
			return;
		}

		// 14. If the scale has a numeric data type and valid values have been
		// set on the scale, the expected range minimum cannot be less than the
		// valid values minimum, and the expected range maximum cannot be larger
		// than the valid values maximum
		if (isNumericType) {
			if (this.getIntegerValueSafe(minValue, 0) < this.getIntegerValueSafe(scale.getMinValue(), 0)) {
				this.addCustomError(errors, "expectedRange.min", OntologyValidator.EXPECTED_MIN_SHOULD_NOT_LESSER_THAN_SCALE_MIN,
						new Object[] { scale.getMinValue(), scale.getMaxValue() });
				return;
			}
			if (this.getIntegerValueSafe(request.getExpectedRange().getMax(), 0) > this.getIntegerValueSafe(scale.getMaxValue(), 0)) {
				this.addCustomError(errors, "expectedRange.max", OntologyValidator.EXPECTED_MAX_SHOULD_NOT_GREATER_THAN_SCALE_MAX,
						new Object[] { scale.getMinValue(), scale.getMaxValue() });
				return;
			}
		}

		// 15. If provided, the expected range minimum must be less than or
		// equal to the expected range maximum, and the expected range maximum
		// must be greater than or equal to the expected range minimum
		if (isNumericType && this.getIntegerValueSafe(request.getExpectedRange().getMin(), 0) > this.getIntegerValueSafe(request.getExpectedRange().getMax(), 0)) {
			this.addCustomError(errors, "expectedRange", OntologyValidator.EXPECTED_MIN_SHOULD_NOT_BE_GREATER_THAN_MAX, null);
		}

		// 16. The combination of property, method and scale should not exist
		this.checkIfMethodPropertyScaleCombination("methodId", request.getMethodId(), request.getPropertyId(), request.getScaleId(), errors);

		// 17. Variable type IDs is required
	  	if(request.getVariableTypeIds().isEmpty()){
		  	this.addCustomError(errors, "variableTypeIds", OntologyValidator.VARIABLE_TYPE_ID_IS_REQUIRED, null);
		}

		if (errors.hasErrors()) {
			return;
		}

	  	// 18. Variable type IDs must be an array of integer values that correspond to the IDs of variable types and contain at least one item
		for (Integer i : request.getVariableTypeIds()) {
			this.shouldHaveValidVariableType("variableTypeIds", i, errors);
		}
	}

	private void variableShouldBeEditable(VariableRequest request, Errors errors) {

		if (request.getId() == null) {
			return;
		}

		try {
			OntologyVariable oldVariable = this.ontologyManagerService.getVariable(request.getProgramUuid(), request.getId());

			// that variable should exist with requestId
			if (Objects.equals(oldVariable, null)) {
				this.addCustomError(errors, OntologyValidator.TERM_DOES_NOT_EXIST, new Object[] { "variable", request.getId() });
				return;
			}

			boolean isEditable = !this.ontologyManagerService.isTermReferred(request.getId());
			if (isEditable) {
				return;
			}

			boolean nameEqual = Objects.equals(request.getName(), oldVariable.getName());
			boolean propertyEqual = Objects.equals(request.getPropertyId(), oldVariable.getProperty().getId());
			boolean methodEqual = Objects.equals(request.getMethodId(), oldVariable.getMethod().getId());
			boolean scaleEqual = Objects.equals(request.getScaleId(), oldVariable.getScale().getId());
			boolean minValuesEqual = Objects.equals(request.getExpectedRange().getMin(), oldVariable.getMinValue());
			boolean maxValuesEqual = Objects.equals(request.getExpectedRange().getMax(), oldVariable.getMaxValue());

			if(!nameEqual){
				this.addCustomError(errors, "name", OntologyValidator.TERM_NOT_EDITABLE,new Object[] { "variable", "name" });
				return;
			}

			if(!propertyEqual){
				this.addCustomError(errors, "propertyId", OntologyValidator.TERM_NOT_EDITABLE,new Object[] { "variable", "property" });
				return;
			}

			if(!methodEqual){
				this.addCustomError(errors, "methodId", OntologyValidator.TERM_NOT_EDITABLE,new Object[] { "variable", "method" });
				return;
			}

			if(!scaleEqual){
				this.addCustomError(errors, "scaleId", OntologyValidator.TERM_NOT_EDITABLE,new Object[] { "variable", "scale" });
				return;
			}

			if(!minValuesEqual || !maxValuesEqual){
				this.addCustomError(errors, "expectedRange", OntologyValidator.TERM_NOT_EDITABLE,new Object[] { "variable", "expectedRange" });
			}

		} catch (Exception e) {
			this.log.error("Error while executing variableShouldBeEditable", e);
			this.addDefaultError(errors);
		}
	}
}
