package org.ibp.api.brapi.v1.trial;

import org.generationcp.middleware.domain.dms.StudySummary;
import org.ibp.api.brapi.v1.study.Contact;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TrialSummaryMapperTest {

	@Test
	public void studySummaryMapperTest() {

		final ModelMapper mapper = TrialSummaryMapper.getInstance();
		final StudySummary studySummary = TrialSummaryTestDataProvider.getTrialSummary();
		final TrialSummary studySummaryDto = mapper.map(studySummary, TrialSummary.class);

		assertThat(studySummary.getLocationId(), equalTo(studySummaryDto.getLocationDbId()));
		assertThat(studySummary.isActive(), equalTo(studySummaryDto.isActive()));
		assertThat(studySummary.getEndDate(), equalTo(studySummaryDto.getEndDate()));
		assertThat(studySummary.getProgramDbId(), equalTo(studySummaryDto.getProgramDbId()));
		assertThat(studySummary.getProgramName(), equalTo(studySummaryDto.getProgramName()));
		assertThat(studySummary.getStartDate(), equalTo(studySummaryDto.getStartDate()));
		assertThat(String.valueOf(studySummary.getTrialDbId()), equalTo(studySummaryDto.getTrialDbId()));
		assertThat(studySummary.getName(), equalTo(studySummaryDto.getTrialName()));
		assertThat(studySummary.getOptionalInfo().size(), equalTo(studySummaryDto.getAdditionalInfo().size()));
		assertThat(studySummary.getInstanceMetaData().size(), equalTo(studySummaryDto.getStudies().size()));
		assertThat(studySummary.getContacts().size(), equalTo(studySummaryDto.getContacts().size()));
		final Contact contact = studySummaryDto.getContacts().get(0);
		assertThat(studySummary.getContacts().get(0).getContactDbId(), equalTo(contact.getContactDbId()));
		assertThat(studySummary.getContacts().get(0).getEmail(), equalTo(contact.getEmail()));
		assertThat(studySummary.getContacts().get(0).getName(), equalTo(contact.getName()));
		assertThat(studySummary.getContacts().get(0).getType(), equalTo(contact.getType()));
	}

}
