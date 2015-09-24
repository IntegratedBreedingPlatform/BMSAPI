
package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.pojos.ListDataProject;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.study.StudyGermplasm;
import org.junit.Assert;
import org.junit.Test;

public class ListDataProjectConverterTest {

	@Test
	public void testConvert() {

		final StudyGermplasm input = new StudyGermplasm();
		input.setEntryNumber(1);
		final GermplasmListEntrySummary summary = new GermplasmListEntrySummary();
		summary.setGid(1);
		summary.setEntryCode("Entry Code");
		summary.setSeedSource("Seed Source");
		summary.setDesignation("Designation");
		summary.setCross("Cross");
		input.setGermplasmListEntrySummary(summary);

		final ListDataProjectConverter converter = new ListDataProjectConverter();
		final ListDataProject output = converter.convert(input);

		Assert.assertEquals(new Integer(0), output.getCheckType());
		Assert.assertEquals(input.getGermplasmListEntrySummary().getGid(), output.getGermplasmId());
		Assert.assertEquals(input.getGermplasmListEntrySummary().getEntryCode(), output.getEntryCode());
		Assert.assertEquals(input.getGermplasmListEntrySummary().getSeedSource(), output.getSeedSource());
		Assert.assertEquals(input.getGermplasmListEntrySummary().getDesignation(), output.getDesignation());
		Assert.assertEquals(input.getGermplasmListEntrySummary().getCross(), output.getGroupName());
		Assert.assertEquals(input.getEntryNumber(), output.getEntryId());
	}
}
