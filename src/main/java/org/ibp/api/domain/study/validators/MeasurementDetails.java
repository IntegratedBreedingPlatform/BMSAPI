
package org.ibp.api.domain.study.validators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ibp.api.domain.ontology.Category;
import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.ontology.ValidValues;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * Data structure to extract and hold details needed to validate a measurement value.
 *
 */
public class MeasurementDetails {

	/**
	 * The variable id we are validating.
	 */
	private final String variableId;

	/**
	 * Variable name that is getting validated.
	 */
	private final String variableName;

	/**
	 * Variable data type.
	 */
	private final DataType variableDataType;

	/**
	 * If categorical then what are the valid values
	 */
	private final ValidValues variableValidValues;

	/**
	 * Measurement value provided by the API call
	 */
	private final String measurementValue;

	/**
	 * For categorical variable this is a map of categorical variable names to term summaries.
	 */
	private final Map<String, Category> mappedCategories = new HashMap<>();

	/**
	 * The id of the measurement. This can be null.
	 */
	private final Integer measurementId;

	public MeasurementDetails(final String variableId, final String variableName, final DataType variableDataType,
			final ValidValues variableValidValues, final Integer measurementId, final String measurementValue) {
		this.variableId = variableId;
		this.variableDataType = variableDataType;
		this.variableValidValues = variableValidValues;
		this.measurementId = measurementId;
		this.measurementValue = measurementValue;
		this.variableName = variableName;

	}

	public DataType getVariableDataType() {
		return this.variableDataType;
	}

	public ValidValues getVariableValidValues() {
		return this.variableValidValues;
	}

	public String getVariableId() {
		return this.variableId;
	}

	public String getMeasurementValue() {
		return this.measurementValue;
	}

	public Integer getMeasurementId() {
		return this.measurementId;
	}

	public Map<String, Category> getMappedCategories() {
		final List<Category> categories = this.variableValidValues.getCategories();
		if (this.variableValidValues != null && this.variableValidValues.getCategories() != null
				&& !this.variableValidValues.getCategories().isEmpty()) {
			return Maps.uniqueIndex(categories, new Function<Category, String>() {
				@Override
				public String apply(final Category category) {
					return category.getName().trim();
				}
			});
		}
		return this.mappedCategories;
	}

	public String getVariableName() {
		return this.variableName;
	}
}
