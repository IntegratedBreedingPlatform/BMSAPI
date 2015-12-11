
package org.ibp.api.domain.study.validators;

import org.ibp.api.domain.ontology.DataType;

public class MeasurementDataValidatorFactoryImpl implements MeasurementDataValidatorFactory {

	@Override
	public DataTypeValidator getMeasurementValidator(final DataType measurementVariableDataType) {
		if (Integer.parseInt(measurementVariableDataType.getId().trim()) == org.generationcp.middleware.domain.ontology.DataType.CATEGORICAL_VARIABLE
				.getId()) {
			return new CategoricalDataTypeValidator();
		} else if (Integer.parseInt(measurementVariableDataType.getId().trim()) == org.generationcp.middleware.domain.ontology.DataType.NUMERIC_VARIABLE
				.getId()) {
			return new NumericVariablDataTypeValidator();
		} else if (Integer.parseInt(measurementVariableDataType.getId().trim()) == org.generationcp.middleware.domain.ontology.DataType.CHARACTER_VARIABLE
				.getId()) {
			return new CharacterVariableDataTypeValidator();

		} else if (Integer.parseInt(measurementVariableDataType.getId().trim()) == org.generationcp.middleware.domain.ontology.DataType.DATE_TIME_VARIABLE
				.getId()) {
			return new DateVariableDataTypeValidator();

		}
		throw new IllegalStateException(
				"The validator factory has been called with an unknow data type. Please contact your administor for further assistance.");
	}
}
