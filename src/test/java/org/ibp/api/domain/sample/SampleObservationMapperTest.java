package org.ibp.api.domain.sample;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SampleObservationMapperTest {

	private static final SimpleDateFormat DATE_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT_3);

	@Test
	public void testSampleObservationMapper() {
		final ModelMapper mapper = SampleObservationMapper.getInstance();
		final SampleDetailsDTO sampleDetailsDTO = createRandomSampleDetails();
		final SampleObservationDto sampleObservationDto = mapper.map(sampleDetailsDTO, SampleObservationDto.class);

		assertThat(sampleObservationDto.getStudyDbId(), equalTo(sampleDetailsDTO.getStudyDbId()));
		assertThat(sampleObservationDto.getLocationDbId(), equalTo(sampleDetailsDTO.getLocationDbId()));
		assertThat(sampleObservationDto.getPlotId(), equalTo(sampleDetailsDTO.getPlotId()));
		assertThat(sampleObservationDto.getPlantId(), equalTo(sampleDetailsDTO.getPlantBusinessKey()));
		assertThat(sampleObservationDto.getSampleId(), equalTo(sampleDetailsDTO.getSampleBusinessKey()));
		assertThat(sampleObservationDto.getTakenBy(), equalTo(sampleDetailsDTO.getTakenBy()));
		assertThat(sampleObservationDto.getSampleDate(), equalTo(DATE_FORMAT.format(sampleDetailsDTO.getSampleDate())));
		assertThat(sampleObservationDto.getSampleType(), equalTo(sampleDetailsDTO.getSampleType()));
		assertThat(sampleObservationDto.getTissueType(), equalTo(sampleDetailsDTO.getTissueType()));
		assertThat(sampleObservationDto.getNotes(), equalTo(sampleDetailsDTO.getNotes()));
		assertThat(sampleObservationDto.getStudyName(), equalTo(sampleDetailsDTO.getStudyName()));
		assertThat(sampleObservationDto.getSeason(), equalTo(sampleDetailsDTO.getSeason()));
		assertThat(sampleObservationDto.getLocationName(), equalTo(sampleDetailsDTO.getLocationName()));
		assertThat(sampleObservationDto.getEntryNumber(), equalTo(sampleDetailsDTO.getEntryNo()));
		assertThat(sampleObservationDto.getPlotNumber(), equalTo(sampleDetailsDTO.getPlotNo()));
		assertThat(sampleObservationDto.getGermplasmDbId(), equalTo(sampleDetailsDTO.getGid()));
		assertThat(sampleObservationDto.getPlantingDate(), equalTo(sampleDetailsDTO.getSeedingDate()));
		assertThat(sampleObservationDto.getHarvestDate(), equalTo(sampleDetailsDTO.getHarvestDate()));
	}

	private static SampleDetailsDTO createRandomSampleDetails() {
		final SampleDetailsDTO sampleDetailsDTO =
			new SampleDetailsDTO(Integer.valueOf(randomNumeric(6)), randomAlphanumeric(6), randomAlphanumeric(6), randomAlphanumeric(6));
		sampleDetailsDTO.setTakenBy(randomAlphanumeric(6));
		sampleDetailsDTO.setSampleDate(new Date());
		sampleDetailsDTO.setSampleType(randomAlphanumeric(6));
		sampleDetailsDTO.setTissueType(randomAlphanumeric(6));
		sampleDetailsDTO.setNotes(randomAlphanumeric(6));
		sampleDetailsDTO.setStudyName(randomAlphanumeric(6));
		sampleDetailsDTO.setSeason(randomAlphanumeric(6));
		sampleDetailsDTO.setLocationName(randomAlphanumeric(6));
		sampleDetailsDTO.setEntryNo(Integer.valueOf(randomNumeric(6)));
		sampleDetailsDTO.setPlotNo(Integer.valueOf(randomNumeric(6)));

		sampleDetailsDTO.setGid(Integer.valueOf(randomNumeric(6)));
		sampleDetailsDTO.setSeedingDate(randomAlphanumeric(6));
		sampleDetailsDTO.setHarvestDate(randomAlphanumeric(6));
		return sampleDetailsDTO;
	}
}
