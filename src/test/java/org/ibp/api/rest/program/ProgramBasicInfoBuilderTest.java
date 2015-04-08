package org.ibp.api.rest.program;

import java.sql.Timestamp;
import java.util.Date;

import junit.framework.TestCase;

import org.ibp.api.domain.program.ProjectBasicInfo;
import org.junit.Assert;
import org.junit.Test;

public class ProgramBasicInfoBuilderTest {

	private final String PROJECT_NAME = "projectName";
	private final String UNIQUE_ID = "98765";
	private final Integer USER_ID = 1;
	private final Date START_DATE = new Timestamp(new Date().getTime());

	@Test
	public void buildWithMandatoryInformation() {
		ProjectBasicInfo built = new ProgramBasicInfoBuilder().projectName(this.PROJECT_NAME)
				.uniqueID(this.UNIQUE_ID).userId(this.USER_ID).startDate(this.START_DATE).build();

		TestCase.assertNull(built.getId());
		Assert.assertEquals(this.PROJECT_NAME, built.getProjectName());
		Assert.assertEquals(this.UNIQUE_ID, built.getUniqueID());
		Assert.assertEquals(this.START_DATE, built.getStartDate());
	}

}
