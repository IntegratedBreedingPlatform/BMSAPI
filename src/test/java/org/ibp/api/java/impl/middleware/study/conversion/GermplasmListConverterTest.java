
package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.domain.study.StudyImportDTO;
import org.junit.Assert;
import org.junit.Test;

public class GermplasmListConverterTest {

	@Test
	public void testConvert() {

		final StudyImportDTO input = new StudyImportDTO();
		input.setName("Test Study");
		input.setStartDate("20150101");
		input.setUserId(1);

		final GermplasmListConverter converter = new GermplasmListConverter();
		final GermplasmList output = converter.convert(input);

		Assert.assertTrue(output.getName().startsWith(input.getName()));
		Assert.assertTrue(output.getDescription().endsWith(input.getName()));
		Assert.assertEquals(input.getStartDate(), output.getDate().toString());
		Assert.assertEquals(new Integer(1), output.getStatus());
		Assert.assertEquals(GermplasmListType.LST.name(), output.getType());
		Assert.assertEquals(input.getUserId(), output.getUserId());
	}
}
