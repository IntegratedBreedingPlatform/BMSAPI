
package org.ibp.api.domain.study.validators;

import org.ibp.api.domain.ontology.DataType;

public interface MeasurementDataValidatorFactory {

	DataTypeValidator getMeasurementValidator(DataType measurementVariableDataType);

}
