
package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.study.Trait;
import org.junit.Assert;
import org.junit.Test;

public class MeasurementVariableConverterTest {

	@Test
	public void testConvert() {

		final Trait input = new Trait();
		input.setTraitId(1);
		input.setTraitName("Plant Height");

		final MeasurementVariableConverter converter = new MeasurementVariableConverter();
		final MeasurementVariable output = converter.convert(input);

		Assert.assertEquals(input.getTraitId(), new Integer(output.getTermId()));
		Assert.assertEquals(input.getTraitName(), output.getName());
		Assert.assertEquals(input.getTraitName(), output.getDescription());
		Assert.assertNull(output.getValue());
		Assert.assertEquals(input.getTraitName(), output.getLabel());
		Assert.assertEquals(PhenotypicType.VARIATE, output.getRole());
		Assert.assertFalse(output.isFactor());

		// Update assertions when TODO related to mapping PMSD in convert method being tested is addressed.
		Assert.assertNull("Looks like you just mapped DataType on MeasurementVariable. Update this assertion.", output.getDataType());
		Assert.assertNull("Looks like you just mapped Property on MeasurementVariable. Update this assertion.", output.getProperty());
		Assert.assertNull("Looks like you just mapped Method type on MeasurementVariable. Update this assertion.", output.getMethod());
		Assert.assertNull("Looks like you just mapped Scale type on MeasurementVariable. Update this assertion.", output.getScale());
	}
}
