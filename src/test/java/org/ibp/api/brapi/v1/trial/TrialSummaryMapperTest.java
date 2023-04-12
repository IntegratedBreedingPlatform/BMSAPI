package org.ibp.api.brapi.v1.trial;

import org.generationcp.middleware.domain.dms.TrialSummary;
import org.ibp.api.brapi.v1.study.Contact;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TrialSummaryMapperTest {

	@Test
	public void studySummaryMapperTest() {

		final ModelMapper mapper = TrialSummaryMapper.getInstance();
		final TrialSummary trialSummary = TrialSummaryTestDataProvider.getTrialSummary();
		final org.ibp.api.brapi.v1.trial.TrialSummary trialSummaryDto = mapper.map(trialSummary, org.ibp.api.brapi.v1.trial.TrialSummary.class);

		assertThat(trialSummary.getLocationId(), equalTo(trialSummaryDto.getLocationDbId()));
		assertThat(trialSummary.isActive(), equalTo(trialSummaryDto.isActive()));
		assertThat(trialSummary.getEndDate(), equalTo(trialSummaryDto.getEndDate()));
		assertThat(trialSummary.getProgramDbId(), equalTo(trialSummaryDto.getProgramDbId()));
		assertThat(trialSummary.getProgramName(), equalTo(trialSummaryDto.getProgramName()));
		assertThat(trialSummary.getStartDate(), equalTo(trialSummaryDto.getStartDate()));
		assertThat(String.valueOf(trialSummary.getTrialDbId()), equalTo(trialSummaryDto.getTrialDbId()));
		assertThat(trialSummary.getName(), equalTo(trialSummaryDto.getTrialName()));
		assertThat(trialSummary.getAdditionalInfo().size(), equalTo(trialSummaryDto.getAdditionalInfo().size()));
		assertThat(trialSummary.getInstanceMetaData().size(), equalTo(trialSummaryDto.getStudies().size()));
		assertThat(trialSummary.getContacts().size(), equalTo(trialSummaryDto.getContacts().size()));
		final Contact contact = trialSummaryDto.getContacts().get(0);
		assertThat(trialSummary.getContacts().get(0).getContactDbId(), equalTo(contact.getContactDbId()));
		assertThat(trialSummary.getContacts().get(0).getEmail(), equalTo(contact.getEmail()));
		assertThat(trialSummary.getContacts().get(0).getName(), equalTo(contact.getName()));
		assertThat(trialSummary.getContacts().get(0).getType(), equalTo(contact.getType()));
		assertThat(trialSummary.getContacts().get(0).getOrcid(), equalTo(contact.getOrcid()));
		assertThat(trialSummary.getContacts().get(0).getInstituteName(), equalTo(contact.getInstituteName()));
	}

}
