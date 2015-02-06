package org.generationcp.bms.program;

import org.generationcp.bms.program.dto.ProjectBasicInfo;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

public class ProgramBasicInfoBuilderTest {

    private final String PROJECT_NAME = "projectName";
    private final String UNIQUE_ID = "98765";
    private final Integer USER_ID = 1;
    private final Date START_DATE = new Timestamp(new Date().getTime());

    @Test
    public void buildWithMandatoryInformation() {
        ProjectBasicInfo built = new ProgramBasicInfoBuilder()
                .projectName(PROJECT_NAME)
                .uniqueID(UNIQUE_ID)
                .userId(USER_ID)
                .startDate(START_DATE)
                .build();

        assertNull(built.getId());
        assertEquals(PROJECT_NAME, built.getProjectName());
        assertEquals(UNIQUE_ID, built.getUniqueID());
        assertEquals(START_DATE, built.getStartDate());
    }

}
