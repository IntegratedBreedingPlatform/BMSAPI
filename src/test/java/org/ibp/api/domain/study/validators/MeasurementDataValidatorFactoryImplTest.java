package org.ibp.api.domain.study.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class MeasurementDataValidatorFactoryImplTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetMeasurementValidatorForCategoricalVariables() throws Exception {
		final MeasurementDataValidatorFactoryImpl measurementDataValidatorFactoryImpl = new MeasurementDataValidatorFactoryImpl();
		final DataTypeValidator measurementValidator = measurementDataValidatorFactoryImpl.getMeasurementValidator(TestValidatorConstants.CATEGORICAL_VARIABLE);
		Assert.assertEquals(measurementValidator.getClass(), CategoricalDataTypeValidator.class);
	}
	
	@Test
	public void testGetMeasurementValidatorForNumbericVariables() throws Exception {
		final MeasurementDataValidatorFactoryImpl measurementDataValidatorFactoryImpl = new MeasurementDataValidatorFactoryImpl();
		final DataTypeValidator measurementValidator =
				measurementDataValidatorFactoryImpl.getMeasurementValidator(TestValidatorConstants.NUMERIC_VARIABLE);
		Assert.assertEquals(measurementValidator.getClass(), NumericVariablDataTypeValidator.class);
	}


	@Test
	public void testGetMeasurementValidatorForCharacterVariables() throws Exception {
		final MeasurementDataValidatorFactoryImpl measurementDataValidatorFactoryImpl = new MeasurementDataValidatorFactoryImpl();
		final DataTypeValidator measurementValidator =
				measurementDataValidatorFactoryImpl.getMeasurementValidator(TestValidatorConstants.CHARACTER_DATA_TYPE);
		Assert.assertEquals(measurementValidator.getClass(), CharacterVariableDataTypeValidator.class);
	}
	
	@Test
	public void testGetMeasurementValidatorForNumericVariables() throws Exception {
		final MeasurementDataValidatorFactoryImpl measurementDataValidatorFactoryImpl = new MeasurementDataValidatorFactoryImpl();
		final DataTypeValidator measurementValidator =
				measurementDataValidatorFactoryImpl.getMeasurementValidator(TestValidatorConstants.DATE_TIME_VARIABLE);
		Assert.assertEquals(measurementValidator.getClass(), DateVariableDataTypeValidator.class);
	}
}
