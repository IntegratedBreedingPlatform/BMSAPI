package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.ontology.NameDescription;
import org.ibp.api.domain.ontology.ScaleRequest;
import org.ibp.api.domain.ontology.ValidValues;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.google.common.base.Strings;

/**
 * Add Scale/Update Scale Validation rules for Scale request Refer:
 * http://confluence.leafnode.io/display/CD/Services+Validation 1. Name is
 * required 2. The name must be unique 3. Data type is required 4. The data type
 * ID must correspond to the ID of one of the supported data types (Numeric,
 * Categorical, Character, DateTime, Person, Location or any other special data
 * type that we add) 5. If the data type is categorical, at least one category
 * must be submitted 6. Categories are only stored if the data type is
 * categorical 7. If there are categories, all labels and values within the set
 * of categories must be unique 8. The min and max valid values are only stored
 * if the data type is numeric 9. If the data type is numeric and minimum and
 * maximum valid values are provided (they are not mandatory), they must be
 * numeric values 10. If present, the minimum valid value must be less than or
 * equal to the maximum valid value, and the maximum valid value must be greater
 * than or equal to the minimum valid value 11. The name, data type and valid
 * values cannot be changed if the scale is already in use 12. Name is no more
 * than 200 characters 13. Description is no more than 255 characters
 */
@Component
public class ScaleRequestValidator extends OntologyValidator implements
org.springframework.validation.Validator {

	static final String CATEGORIES_SHOULD_BE_EMPTY_FOR_NON_CATEGORICAL_DATA_TYPE = "scale.categories.should.not.pass.with.non.categorical.data.type";
	static final String CATEGORIES_NAME_DUPLICATE = "scale.categories.name.duplicate";
	static final String CATEGORIES_DESCRIPTION_DUPLICATE = "scale.categories.description.duplicate";

	@Override
	public boolean supports(Class<?> aClass) {
		return ScaleRequest.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ScaleRequest request = (ScaleRequest) target;

		// 11. The name, data type and valid values cannot be changed if the
		// scale is already in use
		this.scaleShouldBeEditable(request, errors);

		// Need to return from here because we should not check other
		// constraints if request is not required to process
		if (errors.hasErrors()) {
			return;
		}

		// 1. Name is required
		this.shouldNotNullOrEmpty("name", request.getName(), errors);

		if (errors.hasErrors()) {
			return;
		}

		// 12. Name is no more than 200 characters
		this.nameShouldHaveMax200Chars("name", request.getName(), errors);

		// 2. The name must be unique
		this.checkTermUniqueness(request.getId(), request.getName(), CvId.SCALES.getId(), errors);

		// 13. Description is no more than 255 characters
		this.descriptionShouldHaveMax255Chars("description", request.getDescription(), errors);

		// 3. Data type is required
		this.shouldNotNullOrEmpty("dataTypeId", request.getDataTypeId(), errors);

		// 4. The data type ID must correspond to the ID of one of the supported
		// data types (Numeric, Categorical, Character, DateTime, Person,
		// Location or any other special data type that we add)
		this.shouldHaveValidDataType("dataTypeId", request.getDataTypeId(), errors);

		// Need to return from here because other code is dependent on above
		// validation
		if (errors.hasErrors()) {
			return;
		}

		DataType dataType = DataType.getById(request.getDataTypeId());

		ValidValues validValues = request.getValidValues() == null ? new ValidValues() : request
				.getValidValues();

		String minValue = validValues.getMin();
		String maxValue = validValues.getMax();
		List<NameDescription> categories = validValues.getCategories();

		// 5. If the data type is categorical, at least one category must be
		// submitted
		if (Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE)) {
			if (categories == null) {
				this.addCustomError(errors, "validValues.categories",
						OntologyValidator.CATEGORY_SHOULD_HAVE_AT_LEAST_ONE_ITEM, null);
			}
		}

		if (errors.hasErrors()) {
			return;
		}

		// 6. Categories are only stored if the data type is categorical
		if (!Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE)
				&& !this.isNullOrEmpty(categories)) {
			this.addCustomError(errors, "validValues.categories",
					ScaleRequestValidator.CATEGORIES_SHOULD_BE_EMPTY_FOR_NON_CATEGORICAL_DATA_TYPE,
					null);
		}

		// 7. If there are categories, all labels and values within the set of
		// categories must be unique
		this.validateCategoriesForUniqueness(categories, dataType, errors);

		// 8. The min and max valid values are only stored if the data type is
		// numeric
		if (!Objects.equals(dataType, DataType.NUMERIC_VARIABLE)
				&& (minValue != null || maxValue != null)) {
			this.addCustomError(errors, "validValues", OntologyValidator.MIN_MAX_NOT_EXPECTED, null);
		}

		// 9. If the data type is numeric and minimum and maximum valid values
		// are provided (they are not mandatory), they must be numeric values
		if (Objects.equals(dataType, DataType.NUMERIC_VARIABLE)) {
			if (minValue != null && !this.isNonNullValidNumericString(minValue)) {
				this.addCustomError(errors, "validValues.min",
						OntologyValidator.VALUE_SHOULD_BE_NUMERIC, null);
			}

			if (maxValue != null && !this.isNonNullValidNumericString(maxValue)) {
				this.addCustomError(errors, "validValues.max",
						OntologyValidator.VALUE_SHOULD_BE_NUMERIC, null);
			}
		}

		// 10. If present, the minimum valid value must be less than or equal to
		// the maximum valid value, and the maximum valid value must be greater
		// than or equal to the minimum valid value
		if (this.isNonNullValidNumericString(minValue)
				&& this.isNonNullValidNumericString(maxValue)
				&& this.getIntegerValueSafe(minValue, 0) > this.getIntegerValueSafe(maxValue, 0)) {
			this.addCustomError(errors, "validValues.min", OntologyValidator.MIN_MAX_NOT_VALID, null);
		}
	}

	private void validateCategoriesForUniqueness(List<NameDescription> categories,
			DataType dataType, Errors errors) {
		if (categories != null && Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE)) {
			Set<String> labels = new HashSet<>();
			Set<String> values = new HashSet<>();
			for (int i = 0; i < categories.size(); i++) {
				NameDescription nameDescription = categories.get(i);
				String name = nameDescription.getName();
				String value = nameDescription.getDescription();

				if(Strings.isNullOrEmpty(value)){
					this.addCustomError(errors, "validValues.categories[" + i + "].description",
						  OntologyValidator.CATEGORY_DESCRIPTION_IS_NECESSARY, null);
				}

				if(Strings.isNullOrEmpty(name)){
					this.addCustomError(errors, "validValues.categories[" + i + "].name",
						  OntologyValidator.CATEGORY_NAME_IS_NECESSARY, null);
				}

				if (errors.hasErrors()) {
					return;
				}

				if (labels.contains(name)) {
					this.addCustomError(errors, "validValues.categories[" + i + "].name",
							ScaleRequestValidator.CATEGORIES_NAME_DUPLICATE, null);
				} else {
					labels.add(nameDescription.getName());
				}

				if (values.contains(value)) {
					this.addCustomError(errors, "validValues.categories[" + i + "].description",
							ScaleRequestValidator.CATEGORIES_DESCRIPTION_DUPLICATE, null);
				} else {
					values.add(nameDescription.getDescription());
				}
			}
		}
	}

	private void scaleShouldBeEditable(ScaleRequest request, Errors errors) {
		if (request.getId() == null) {
			return;
		}

		try {
			Scale oldScale = this.ontologyManagerService.getScaleById(request.getId());

			// that method should exist with requestId
			if (Objects.equals(oldScale, null)) {
				this.addCustomError(errors, OntologyValidator.TERM_DOES_NOT_EXIST, new Object[] {
						"scale", request.getId() });
				return;
			}

			boolean isEditable = !this.ontologyManagerService.isTermReferred(request.getId());
			if (isEditable) {
				return;
			}

			boolean isNameSame = Objects.equals(request.getName(), oldScale.getName());
			if (!isNameSame) {
				this.addCustomError(errors, "name", OntologyValidator.TERM_NOT_EDITABLE,
						new Object[] { "scale", "name" });
			}

			boolean isDataTypeSame = Objects.equals(request.getDataTypeId(),
					this.getDataTypeIdSafe(oldScale.getDataType()));
			if (!isDataTypeSame) {
				this.addCustomError(errors, "dataTypeId", OntologyValidator.TERM_NOT_EDITABLE,
						new Object[] { "scale", "dataTypeId" });
			}

			ValidValues validValues = request.getValidValues() == null ? new ValidValues()
			: request.getValidValues();
			boolean minValuesAreEqual = Objects.equals(validValues.getMin(), oldScale.getMinValue());
			boolean maxValuesAreEqual = Objects.equals(validValues.getMax(), oldScale.getMaxValue());
			List<NameDescription> categories = validValues.getCategories() == null ? new ArrayList<NameDescription>()
					: validValues.getCategories();
			boolean categoriesEqualSize = Objects.equals(categories.size(), oldScale
					.getCategories().size());
			boolean categoriesValuesAreSame = true;
			if (categoriesEqualSize) {
				for (NameDescription l : categories) {
					if (oldScale.getCategories().containsKey(l.getName())
							&& Objects.equals(oldScale.getCategories().get(l.getName()),
									l.getDescription())) {
						continue;
					}
					categoriesValuesAreSame = false;
					break;
				}
			}
			if (!minValuesAreEqual || !maxValuesAreEqual || !categoriesEqualSize
					|| !categoriesValuesAreSame) {
				this.addCustomError(errors, "validValues", OntologyValidator.TERM_NOT_EDITABLE,
						new Object[] { "scale", "validValues" });
			}

		} catch (MiddlewareQueryException e) {
			this.log.error("Error while executing scaleShouldBeEditable", e);
			this.addDefaultError(errors);
		}
	}

	private Integer getDataTypeIdSafe(DataType dataType) {
		return dataType == null ? null : dataType.getId();
	}
}
