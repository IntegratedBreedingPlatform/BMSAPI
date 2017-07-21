package org.ibp.api.brapi.v1.trial;

import org.generationcp.middleware.domain.dms.StudySummary;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TrialSummaryMapperTest {

	@Test
	public void trialSummaryMapperTest() {

		final ModelMapper mapper = TrialSummaryMapper.getInstance();
		final StudySummary studySummary = TrialSummaryTestDataProvider.getTrialSummary();
		final TrialSummary trialSummaryDto = mapper.map(studySummary, TrialSummary.class);

		assertThat(studySummary.getLocationId(), equalTo(trialSummaryDto.getLocationDbId()));
		assertThat(studySummary.isActive(), equalTo(trialSummaryDto.isActive()));
		assertThat(studySummary.getEndDate(), equalTo(trialSummaryDto.getEndDate()));
		assertThat(studySummary.getProgramDbId(), equalTo(trialSummaryDto.getProgramDbId()));
		assertThat(studySummary.getProgramName(), equalTo(trialSummaryDto.getProgramName()));
		assertThat(studySummary.getStartDate(), equalTo(trialSummaryDto.getStartDate()));
		assertThat(studySummary.getStudyDbid(), equalTo(trialSummaryDto.getTrialDbId()));
		assertThat(studySummary.getName(), equalTo(trialSummaryDto.getTrialName()));
		assertThat(studySummary.getOptionalInfo().size(), equalTo(trialSummaryDto.getAdditionalInfo().size()));
		assertThat(studySummary.getInstanceMetaData().size(), equalTo(trialSummaryDto.getStudies().size()));
	}

}
