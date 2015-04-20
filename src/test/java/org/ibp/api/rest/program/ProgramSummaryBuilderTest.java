package org.ibp.api.rest.program;

import junit.framework.TestCase;

import org.ibp.api.domain.program.ProgramSummary;
import org.junit.Assert;
import org.junit.Test;

public class ProgramSummaryBuilderTest {

	private final String PROJECT_NAME = "projectName";
	private final String UNIQUE_ID = "98765";
	private final String USER_ID = "1";
	private final String START_DATE = "2015-01-01";

	@Test
	public void buildWithMandatoryInformation() {
		ProgramSummary built = new ProgramSummaryBuilder().projectName(this.PROJECT_NAME)
				.uniqueID(this.UNIQUE_ID).userId(this.USER_ID).startDate(this.START_DATE).build();

		TestCase.assertNull(built.getId());
		Assert.assertEquals(this.PROJECT_NAME, built.getProjectName());
		Assert.assertEquals(this.UNIQUE_ID, built.getUniqueID());
		Assert.assertEquals(this.USER_ID, built.getUserId());
		Assert.assertEquals(this.START_DATE, built.getStartDate());
	}

}
