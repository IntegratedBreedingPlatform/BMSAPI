package org.ibp.api.domain.sample;

import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.ibp.api.java.impl.middleware.SampleTestDataGenerator;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SampleObservationMapperTest {


	@Test
	public void testSampleObservationMapper() {
		final ModelMapper mapper = SampleObservationMapper.getInstance();
		final SampleDetailsDTO sampleDetailsDTO = SampleTestDataGenerator.createRandomSampleDetails();
		final SampleObservationDto sampleObservationDto = mapper.map(sampleDetailsDTO, SampleObservationDto.class);

		assertThat(sampleObservationDto.getGermplasmDbId(), equalTo(sampleDetailsDTO.getGid()));
		assertThat(sampleObservationDto.getNotes(), equalTo(sampleDetailsDTO.getNotes()));
		assertThat(sampleObservationDto.getObservationUnitDbId(), equalTo(sampleDetailsDTO.getObsUnitId()));
		assertThat(sampleObservationDto.getPlateDbId(), equalTo(sampleDetailsDTO.getPlateId()));
		assertThat(sampleObservationDto.getSampleDbId(), equalTo(sampleDetailsDTO.getSampleBusinessKey()));
		assertThat(sampleObservationDto.getSampleTimestamp(), equalTo(sampleDetailsDTO.getSampleDate()));
		assertThat(sampleObservationDto.getStudyDbId(), equalTo(sampleDetailsDTO.getInstanceId()));
		assertThat(sampleObservationDto.getTakenBy(), equalTo(sampleDetailsDTO.getTakenBy()));
		assertThat(sampleObservationDto.getTissueType(), equalTo(sampleDetailsDTO.getTissueType()));
	}

}
