package org.ibp.api.brapi.v1.study;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import java.text.ParseException;

public class StudyMapperTest {

	@Test
	public void studyDetailsMapperTest() throws ParseException {

		final ModelMapper mapper = StudyMapper.getInstance();
		final StudyDetailsDto studyDetailsDto = StudyTestDataProvider.getStudyDetailsDto();
		final StudyDetailsData studyDetailsData = mapper.map(studyDetailsDto, StudyDetailsData.class);

		assertThat(studyDetailsData.getStudyDbId(), equalTo(studyDetailsDto.getMetadata().getStudyDbId()));
		assertThat(studyDetailsData.getStudyName(), equalTo(studyDetailsDto.getMetadata().getStudyName()));
		assertThat(studyDetailsData.getSeasons().get(0), equalTo(studyDetailsDto.getMetadata().getSeasons().get(0)));
		assertThat(studyDetailsData.getTrialDbId(), equalTo(studyDetailsDto.getMetadata().getTrialDbId()));
		assertThat(studyDetailsData.getTrialName(), equalTo(studyDetailsDto.getMetadata().getTrialName()));
		assertThat(studyDetailsData.getStartDate(), equalTo(studyDetailsDto.getMetadata().getStartDate()));
		assertThat(studyDetailsData.getEndDate(), equalTo(studyDetailsDto.getMetadata().getEndDate()));
		assertThat(studyDetailsData.getActive(), equalTo(studyDetailsDto.getMetadata().getActive()));
		assertThat(studyDetailsData.getContacts().size(), equalTo(studyDetailsDto.getContacts().size()));

	}

}
