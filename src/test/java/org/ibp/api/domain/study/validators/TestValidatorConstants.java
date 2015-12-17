
package org.ibp.api.domain.study.validators;

import java.util.UUID;

import org.ibp.api.domain.ontology.DataType;

public class TestValidatorConstants {

	static final Integer TEST_MEASUREMENT_ID = new Integer(200);

	static final String TERM_SUMMART_ID = "500";

	static final String TERM_SUMMARY_NAME = "Term Summary";

	static final String TERM_SUMMARY_DEFINITION = "Term Summary Definition";

	static final int TEST_MEASUREMENT_INDEX = 0;

	static final String TEST_MEASUREMENT_VALUE = "Test Measurement Value";

	static final int TEST_OBSERVATION_IDENTIFIER = 100;

	static final String TEST_MEASUREMENT_VARIABLE_NAME = "Test Variable Name";

	static final String TEST_MEASUREMENT_VARIABLE_ID = "1";

	static final DataType CHARACTER_DATA_TYPE = new DataType("1120", "Character", false);

	static final DataType NUMERIC_VARIABLE = new DataType("1110", "Numeric", false);

	static final DataType DATE_TIME_VARIABLE = new DataType("1117", "Date", false);

	static final DataType CATEGORICAL_VARIABLE = new DataType("1130", "Categorical", false);

	static String CROP_NAME = "wheat";

	static String PROGRAM_ID = UUID.randomUUID().toString();

	static String STUDY_ID = "1";
}
