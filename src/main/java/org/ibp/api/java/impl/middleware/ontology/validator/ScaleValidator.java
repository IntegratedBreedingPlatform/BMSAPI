
package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.pojos.oms.VariableOverrides;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.ontology.Category;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.ontology.Usage;
import org.ibp.api.domain.ontology.ValidValues;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.generationcp.middleware.domain.ontology.DataType.CATEGORICAL_VARIABLE;
import static org.generationcp.middleware.domain.ontology.DataType.NUMERIC_VARIABLE;

/**
 * Add Scale/Update Scale Validation rules for Scale request Refer: http://confluence.leafnode.io/display/CD/Services+Validation 1. Name is
 * required 2. The name must be unique 3. Data type is required 4. The data type ID must correspond to the ID of one of the supported data
 * types (Numeric, Categorical, Character, DateTime, Person, Location or any other special data type that we add) 5. If the data type is
 * categorical, at least one category must be submitted 6. Categories are only stored if the data type is categorical 7. If there are
 * categories, all labels and values within the set of categories must be unique 8. The min and max valid values are only stored if the data
 * type is numeric 9. If the data type is numeric and minimum and maximum valid values are provided (they are not mandatory), they must be
 * numeric values 10. If present, the minimum valid value must be less than or equal to the maximum valid value, and the maximum valid value
 * must be greater than or equal to the minimum valid value 11. The name, data type and valid values cannot be changed if the scale is
 * already in use 12. Name is no more than 200 characters 13. Description is no more than 1024 characters
 */

/**
 * Extended from {@link OntologyValidator} for basic validation functions and error messages
 */

@Component
public class ScaleValidator extends OntologyValidator implements org.springframework.validation.Validator {

	private static final Integer NAME_TEXT_LIMIT = 200;
	private static final Integer CATEGORY_VALUE_TEXT_LIMIT = 255;
	private static final Integer DESCRIPTION_TEXT_LIMIT = 1024;

	private static final String SCALE_CATEGORIES_NAME_DUPLICATE = "scale.category.name.duplicate";
	private static final String SCALE_CATEGORIES_DESCRIPTION_DUPLICATE = "scale.category.description.duplicate";
	private static final String SCALE_CATEGORY_DESCRIPTION_REQUIRED = "scale.category.description.required";
	private static final String SCALE_NAME_DESCRIPTION_REQUIRED = "scale.category.name.required";
	private static final String SCALE_SYSTEM_DATA_TYPE = "scale.system.datatype";
	private static final String SCALE_CATEGORY_NOT_ALLOWED_TO_EDIT = "scale.category.measured.updated";
	private static final String SCALE_CATEGORY_NOT_ALLOWED_TO_DELETE = "scale.category.measured.deleted";
	protected static final String SCALE_RANGE_NOT_VALID = "scale.range.not.valid";

	private static final Logger LOGGER = LoggerFactory.getLogger(ScaleValidator.class);

	private static final String ERROR_NAME = "dataTypeId";
	private static final String NAME = "name";
	private static final String SCALE_NAME = "scale";
	private static final String DESCRIPTION = "description";


	@Override
	public boolean supports(Class<?> aClass) {
		return ScaleDetails.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ScaleDetails scaleDetails = (ScaleDetails) target;

		boolean nameValidationResult = this.nameValidationProcessor(scaleDetails, errors);

		this.descriptionValidationProcessor(scaleDetails, errors);

		boolean dataTypeValidationResult = this.dataTypeValidationProcessor(scaleDetails, errors);

		Integer dataTypeId = this.parseDataTypeIdAsInteger(scaleDetails.getDataType());

		if (dataTypeValidationResult) {

			if (Objects.equals(dataTypeId, DataType.CATEGORICAL_VARIABLE.getId())) {
				this.categoricalDataTypeValidationProcessor(scaleDetails, errors);
			}
			if (Objects.equals(dataTypeId, DataType.NUMERIC_VARIABLE.getId())) {
				this.numericDataTypeValidationProcessor(scaleDetails, errors);
			}
		}

		if (nameValidationResult) {
			this.scaleShouldBeEditable(scaleDetails, errors);
		}
	}

	private void validateCategoriesForUniqueness(List<org.ibp.api.domain.ontology.Category> categories, DataType dataType, Errors errors) {
		if (categories != null && Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE)) {
			Set<String> labels = new HashSet<>();
			Set<String> values = new HashSet<>();

			for (int i = 1; i <= categories.size(); i++) {

				org.ibp.api.domain.ontology.TermSummary category = categories.get(i - 1);
				String name = category.getName().trim();
				String value = category.getDescription().trim();
				String errorNamePrefix = "validValues.categories[" + i + "].";

				if (this.isNullOrEmpty(value)) {
					this.addCustomError(errors, errorNamePrefix + DESCRIPTION, ScaleValidator.SCALE_CATEGORY_DESCRIPTION_REQUIRED, null);
				}

				if (this.isNullOrEmpty(name)) {
					this.addCustomError(errors, errorNamePrefix + NAME, ScaleValidator.SCALE_NAME_DESCRIPTION_REQUIRED, null);
				}

				if (errors.hasErrors()) {
					return;
				}

				this.fieldShouldNotOverflow(errorNamePrefix + NAME, name, ScaleValidator.NAME_TEXT_LIMIT, errors);

				this.fieldShouldNotOverflow(errorNamePrefix + DESCRIPTION, value, ScaleValidator.CATEGORY_VALUE_TEXT_LIMIT, errors);

				if (errors.hasErrors()) {
					return;
				}

				if (labels.contains(name)) {
					this.addCustomError(errors, errorNamePrefix + NAME, ScaleValidator.SCALE_CATEGORIES_NAME_DUPLICATE, null);
				} else {
					labels.add(category.getName().trim());
				}

				if (values.contains(value)) {
					this.addCustomError(errors, errorNamePrefix + DESCRIPTION, ScaleValidator.SCALE_CATEGORIES_DESCRIPTION_DUPLICATE, null);
				} else {
					values.add(category.getDescription().trim());
				}
			}
		}
	}

	private void validateUpdatedCategoriesNotMeasured(ScaleDetails oldScale, ScaleDetails scaleDetails, Errors errors) {

		if (Objects.equals(scaleDetails.getDataType().getId(), String.valueOf(CATEGORICAL_VARIABLE.getId()))) {
			final Map<String, Category> oldCategories =
					Maps.uniqueIndex(oldScale.getValidValues().getCategories(), new Function<Category, String>() {

						@Override public String apply(final Category from) {
							return from.getId();
						}
					});
			// Comparing that categories that were updated do not have any relation in phenotype table
			for (Category category : scaleDetails.getValidValues().getCategories()) {
				if (category.getId() != null) {
					Category savedCategory = oldCategories.get(category.getId());
					if (savedCategory != null && !category.getName().equals(savedCategory.getName()) && !savedCategory.isEditable()) {
						this.addCustomError(errors, "validValues", ScaleValidator.SCALE_CATEGORY_NOT_ALLOWED_TO_EDIT,
								new Object[] {"scale", "ValidValues"});
						return;
					}
				}
			}
			Predicate<Category> predicate = new Predicate<Category>() {

				@Override public boolean apply(Category input) {
					return !Strings.isNullOrEmpty(input.getId());
				}
			};
			final Map<String, Category> newCategories =
					Maps.uniqueIndex(Collections2.filter(scaleDetails.getValidValues().getCategories(), predicate),
							new Function<Category, String>() {

								@Override public String apply(final Category from) {
									return from.getId();
								}
							});
			// Comparing that none of the categories that have relations in phenotype are going to be deleted
			for (Category category : oldScale.getValidValues().getCategories()) {
				if (!category.isEditable() && !newCategories.containsKey(category.getId())) {
					this.addCustomError(errors, "validValues", ScaleValidator.SCALE_CATEGORY_NOT_ALLOWED_TO_DELETE,
							new Object[] {"scale", "ValidValues"});
					return;
				}
			}
		}
	}

	private void scaleShouldBeEditable(final ScaleDetails scaleDetails, final Errors errors) {
		if (scaleDetails.getId() == null) {
			return;
		}

		final Integer initialCount = errors.getErrorCount();

		try {
			final ScaleDetails oldScale = this.scaleService.getScaleById(scaleDetails.getId());

			// that method should exist with requestId
			if (Objects.equals(oldScale, null)) {
				this.addCustomError(errors, BaseValidator.ID_DOES_NOT_EXIST, new Object[] {"Scale", scaleDetails.getId()});
				return;
			}

			if (oldScale.getDataType() != null && oldScale.getDataType().isSystemDataType()) {
				this.addCustomError(errors, ScaleValidator.ERROR_NAME, ScaleValidator.SCALE_SYSTEM_DATA_TYPE, null);
			}

			this.validateUpdatedCategoriesNotMeasured(oldScale, scaleDetails, errors);

			this.validateUpdatedRanges(scaleDetails, oldScale, errors);

			if (errors.getErrorCount() > initialCount) {
				return;
			}

			final boolean referred = this.termDataManager.isTermReferred(StringUtil.parseInt(scaleDetails.getId(), null));
			if (referred) {
				if (Objects.equals(scaleDetails.getDataType().getId(), String.valueOf(CATEGORICAL_VARIABLE.getId()))
						|| Objects.equals(scaleDetails.getDataType().getId(), String.valueOf(NUMERIC_VARIABLE.getId()))) {
					return;
				}
			} else {
				return;
			}

			final boolean isNameSame = Objects.equals(scaleDetails.getName(), oldScale.getName());
			if (!isNameSame) {
				this.addCustomError(errors, ScaleValidator.NAME, BaseValidator.RECORD_IS_NOT_EDITABLE,
						new Object[] {ScaleValidator.SCALE_NAME, "Name"});
			}

			final Integer newDataTypeId = this.parseDataTypeIdAsInteger(scaleDetails.getDataType());
			final Integer oldDataTypeId = this.parseDataTypeIdAsInteger(oldScale.getDataType());

			final boolean isDataTypeSame = Objects.equals(newDataTypeId, oldDataTypeId);

			if (!isDataTypeSame) {
				this.addCustomError(errors, ScaleValidator.ERROR_NAME, BaseValidator.RECORD_IS_NOT_EDITABLE,
						new Object[] {ScaleValidator.SCALE_NAME, "DataTypeId"});
			}

			final ValidValues validValues = scaleDetails.getValidValues() == null ? new ValidValues() : scaleDetails.getValidValues();

			boolean minValuesAreEqual = true;
			boolean maxValuesAreEqual = true;
			boolean categoriesEqualSize = true;
			boolean categoriesValuesAreSame = true;

			final DataType dataType = DataType.getById(newDataTypeId);

			if (Objects.equals(dataType, DataType.NUMERIC_VARIABLE)) {

				// Note: Check if oldScale min-max and validValues min-max null or empty. If empty then do not check for equality.

				if (!(this.isNullOrEmpty(validValues.getMin()) && this.isNullOrEmpty(oldScale.getValidValues().getMin()))) {
					minValuesAreEqual = Objects.equals(validValues.getMin(), oldScale.getValidValues().getMin());
				}

				if (!(this.isNullOrEmpty(validValues.getMax()) && this.isNullOrEmpty(oldScale.getValidValues().getMax()))) {
					maxValuesAreEqual = Objects.equals(validValues.getMax(), oldScale.getValidValues().getMax());
				}

			} else if (Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE)) {
				final List<org.ibp.api.domain.ontology.Category> categories = validValues.getCategories() == null
						? new ArrayList<org.ibp.api.domain.ontology.Category>() : validValues.getCategories();
				categoriesEqualSize = Objects.equals(categories.size(), oldScale.getValidValues().getCategories().size());
				categoriesValuesAreSame = true;

				if (categoriesEqualSize) {

					// Converting old categories to Map for comparing.
					final Map<String, String> oldCategories = new HashMap<>();
					for (final Category t : oldScale.getValidValues().getCategories()) {
						oldCategories.put(t.getName(), t.getDescription());
					}

					for (final Category l : categories) {
						if (oldCategories.containsKey(l.getName()) && Objects.equals(oldCategories.get(l.getName()), l.getDescription())) {
							continue;
						}
						categoriesValuesAreSame = false;
						break;
					}
				}

			}

			if (!minValuesAreEqual || !maxValuesAreEqual || !categoriesEqualSize || !categoriesValuesAreSame) {
				this.addCustomError(errors, "validValues", BaseValidator.RECORD_IS_NOT_EDITABLE, new Object[] {"scale", "ValidValues"});
			}

		} catch (final MiddlewareException e) {
			ScaleValidator.LOGGER.error("Error while executing scaleShouldBeEditable", e);
			this.addDefaultError(errors);
		}
	}

	private void validateUpdatedRanges(final ScaleDetails scaleDetails, final ScaleDetails oldScale, final Errors errors) {
		if (Objects.equals(scaleDetails.getDataType().getId(), String.valueOf(NUMERIC_VARIABLE.getId()))) {
			Usage usage = oldScale.getMetadata().getUsage();
			List<TermSummary> variables = usage.getVariables();
			if (usage != null && variables != null && variables.size() > 0) {
				final List<Integer> variablesIds = this.getVariablesIds(variables);

				if (variablesIds != null && variablesIds.size() > 0) {

					// If present, the minimum and maximun valid value must be between the min and max values of the scale that variable
					// belongs
					final String min = scaleDetails.getValidValues().getMin();
					final String max = scaleDetails.getValidValues().getMax();
					if (min != null && max != null
						&& !this.checkScaleRangesWithVariableRanges(min, max, variablesIds)) {
						this.addCustomError(errors, "validValues", ScaleValidator.SCALE_RANGE_NOT_VALID,
								new Object[] {"scale", "ValidValues"});

					}
				}
			}

		}
	}

	private boolean nameValidationProcessor(ScaleDetails scaleDetails, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		// 1. Name is required
		this.shouldNotNullOrEmpty("Name", ScaleValidator.NAME, scaleDetails.getName(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 12. Name is no more than 200 characters
		this.fieldShouldNotOverflow(ScaleValidator.NAME, scaleDetails.getName(), ScaleValidator.NAME_TEXT_LIMIT, errors);

		// 2. The name must be unique
		this.checkTermUniqueness("Scale", StringUtil.parseInt(scaleDetails.getId(), null), scaleDetails.getName(), CvId.SCALES.getId(),
				errors);

		return errors.getErrorCount() == initialCount;
	}

	// 13. Description is no more than 1024 characters
	private boolean descriptionValidationProcessor(ScaleDetails scaleDetails, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		if (Strings.isNullOrEmpty(scaleDetails.getDescription())) {
			scaleDetails.setDescription("");
		} else {
			scaleDetails.setDescription(scaleDetails.getDescription().trim());
		}

		this.fieldShouldNotOverflow("description", scaleDetails.getDescription(), ScaleValidator.DESCRIPTION_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean dataTypeValidationProcessor(ScaleDetails scaleDetails, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		// 3. Data type is required
		this.shouldNotNullOrEmpty("Data Type", ScaleValidator.ERROR_NAME, scaleDetails.getDataType(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		if (!this.isNonNullValidNumericString(scaleDetails.getDataType().getId())) {
			this.addCustomError(errors, ScaleValidator.ERROR_NAME, BaseValidator.INVALID_TYPE_ID, new Object[] {"Data Type"});
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		Integer dataTypeId = this.parseDataTypeIdAsInteger(scaleDetails.getDataType());

		// 4. The data type ID must correspond to the ID of one of the supported
		// data types (Numeric, Categorical, Character, DateTime, Person,
		// Location or any other special data type that we add)
		if (DataType.getById(dataTypeId) == null) {
			this.addCustomError(errors, ScaleValidator.ERROR_NAME, BaseValidator.INVALID_TYPE_ID, new Object[] {"Data Type"});
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		if (Objects.equals(DataType.getById(dataTypeId).isSystemDataType(), true)) {
			this.addCustomError(errors, ScaleValidator.ERROR_NAME, ScaleValidator.SCALE_SYSTEM_DATA_TYPE, null);
		}

		return errors.getErrorCount() == initialCount;
	}

	private boolean categoricalDataTypeValidationProcessor(ScaleDetails scaleDetails, Errors errors) {

		Integer initialCount = errors.getErrorCount();

		DataType dataType = DataType.getById(this.parseDataTypeIdAsInteger(scaleDetails.getDataType()));

		ValidValues validValues = scaleDetails.getValidValues() == null ? new ValidValues() : scaleDetails.getValidValues();

		List<org.ibp.api.domain.ontology.Category> categories = validValues.getCategories();

		// 5. If the data type is categorical, at least one category must be
		// submitted
		if (Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE)) {
			if (categories == null || categories.isEmpty()) {
				this.addCustomError(errors, "validValues.categories", BaseValidator.LIST_SHOULD_NOT_BE_EMPTY, new Object[] {"category"});
			}
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 7. If there are categories, all labels and values within the set of
		// categories must be unique
		this.validateCategoriesForUniqueness(categories, dataType, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean numericDataTypeValidationProcessor(final ScaleDetails scaleDetails, final Errors errors) {

		final Integer initialCount = errors.getErrorCount();

		final DataType dataType = DataType.getById(this.parseDataTypeIdAsInteger(scaleDetails.getDataType()));

		final ValidValues validValues = scaleDetails.getValidValues() == null ? new ValidValues() : scaleDetails.getValidValues();

		BigDecimal min = null;
		BigDecimal max = null;

		final String minValue = validValues.getMin() == null ? null : validValues.getMin();
		final String maxValue = validValues.getMax() == null ? null : validValues.getMax();

		// 9. If the data type is numeric and minimum and maximum valid values
		// are provided (they are not mandatory), they must be numeric values
		if (Objects.equals(dataType, DataType.NUMERIC_VARIABLE)) {
			if (!this.isNullOrEmpty(minValue)) {
				min = StringUtil.parseBigDecimal(minValue, null);
				if (min == null) {
					this.addCustomError(errors, "validValues.min", BaseValidator.FIELD_SHOULD_BE_NUMERIC, null);
				}
			}

			if (!this.isNullOrEmpty(maxValue)) {
				max = StringUtil.parseBigDecimal(maxValue, null);
				if (max == null) {
					this.addCustomError(errors, "validValues.max", BaseValidator.FIELD_SHOULD_BE_NUMERIC, null);
				}
			}

		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 10. If present, the minimum valid value must be less than or equal to
		// the maximum valid value, and the maximum valid value must be greater
		// than or equal to the minimum valid value

		if (min != null && max != null && min.compareTo(max) != -1) {
			this.addCustomError(errors, "validValues.min", BaseValidator.MIN_MAX_NOT_VALID, null);
		}

		return errors.getErrorCount() == initialCount;
	}

	private Integer parseDataTypeIdAsInteger(org.ibp.api.domain.ontology.DataType dataType) {
		if (dataType == null) {
			return null;
		}
		return StringUtil.parseInt(dataType.getId(), null);
	}

	private boolean checkScaleRangesWithVariableRanges(final String minValue, final String maxValue,
			final List<Integer> variablesIds) {
		boolean ok = true;

		final List<VariableOverrides> overrides = this.ontologyVariableDataManager.getVariableOverridesByVariableIds(variablesIds);

		final Iterator<VariableOverrides> it = overrides.iterator();
		while (it.hasNext() && ok) {
			final VariableOverrides override = it.next();
			final BigDecimal scaleMinValue = StringUtil.parseBigDecimal(minValue, null);
			final BigDecimal scaleMaxValue = StringUtil.parseBigDecimal(maxValue, null);
			final BigDecimal overrideMinValue = StringUtil.parseBigDecimal(override.getExpectedMin(), null);
			final BigDecimal overrideMaxValue = StringUtil.parseBigDecimal(override.getExpectedMax(), null);
			if (scaleMinValue != null && scaleMaxValue != null && overrideMinValue != null && overrideMaxValue != null
					&& !(scaleMinValue.compareTo(overrideMinValue) <= 0 && scaleMaxValue.compareTo(overrideMaxValue) >= 0)) {
				// variable expected range not included in scale range
				ok = false;
			}

		}
		return ok;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> getVariablesIds(final List<org.ibp.api.domain.ontology.TermSummary> list) {
		return (List<Integer>) CollectionUtils.collect(list, new Transformer() {

			@Override
			public Integer transform(final Object input) {
				final org.ibp.api.domain.ontology.TermSummary variable = (org.ibp.api.domain.ontology.TermSummary) input;
				return Integer.valueOf(variable.getId());
			}

		});
	}
}
