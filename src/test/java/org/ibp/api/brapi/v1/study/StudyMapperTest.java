package org.ibp.api.brapi.v1.study;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.generationcp.middleware.service.api.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.ibp.api.brapi.v1.location.Location;
import org.junit.Test;
import org.modelmapper.ModelMapper;

public class StudyMapperTest {

	@Test
	public void studyDetailsMapperTest() {

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

	@Test
	public void locationDetailsMapperTest() {
		final ModelMapper mapper = StudyMapper.getInstance();
		final LocationDetailsDto locationDetailsDto = StudyTestDataProvider.getLocationDetailsDto();
		final Location location = mapper.map(locationDetailsDto, Location.class);

		assertThat(location.getAbbreviation(), equalTo(locationDetailsDto.getAbbreviation()));
		assertThat(location.getAltitude(), equalTo(locationDetailsDto.getAltitude()));
		assertThat(location.getCountryCode(), equalTo(locationDetailsDto.getCountryCode()));
		assertThat(location.getCountryName(), equalTo(locationDetailsDto.getCountryName()));
		assertThat(location.getLongitude(), equalTo(locationDetailsDto.getLongitude()));
		assertThat(location.getLatitude(), equalTo(locationDetailsDto.getLatitude()));
		assertThat(location.getLocationDbId(), equalTo(locationDetailsDto.getLocationDbId()));
		assertThat(location.getLocationType(), equalTo(locationDetailsDto.getLocationType()));
		assertThat(location.getName(), equalTo(locationDetailsDto.getName()));
	}

}
