package org.ibp.api.domain.sample;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.ibp.api.java.impl.middleware.SampleTestDataGenerator;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import java.text.SimpleDateFormat;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SampleObservationMapperTest {

	private static final SimpleDateFormat DATE_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT_3);

	@Test
	public void testSampleObservationMapper() {
		final ModelMapper mapper = SampleObservationMapper.getInstance();
		final SampleDetailsDTO sampleDetailsDTO = SampleTestDataGenerator.createRandomSampleDetails();
		final SampleObservationDto sampleObservationDto = mapper.map(sampleDetailsDTO, SampleObservationDto.class);

		assertThat(sampleObservationDto.getStudyDbId(), equalTo(sampleDetailsDTO.getStudyDbId()));
		assertThat(sampleObservationDto.getObservationUnitDbId(), equalTo(sampleDetailsDTO.getObsUnitId()));
		assertThat(sampleObservationDto.getSampleDbId(), equalTo(sampleDetailsDTO.getSampleBusinessKey()));
		assertThat(sampleObservationDto.getTakenBy(), equalTo(sampleDetailsDTO.getTakenBy()));
		assertThat(sampleObservationDto.getSampleType(), equalTo(sampleDetailsDTO.getSampleType()));
		assertThat(sampleObservationDto.getTissueType(), equalTo(sampleDetailsDTO.getTissueType()));
		assertThat(sampleObservationDto.getNotes(), equalTo(sampleDetailsDTO.getNotes()));
		assertThat(sampleObservationDto.getGermplasmDbId(), equalTo(sampleDetailsDTO.getGermplasmUUID()));
		assertThat(sampleObservationDto.getSampleTimestamp(), equalTo(sampleDetailsDTO.getSampleDate()));
		assertThat(sampleObservationDto.getPlateDbId(), equalTo(sampleDetailsDTO.getPlateId()));
		assertThat(sampleObservationDto.getPlateIndex(), equalTo(sampleDetailsDTO.getSampleNumber()));
		assertThat(sampleObservationDto.getPlotDbId(), equalTo(sampleDetailsDTO.getPlotNo().toString()));
	}

}
