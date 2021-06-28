package org.ibp.api.java.impl.middleware;

import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.ibp.api.domain.sample.SampleObservationDto;

import java.util.Date;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

public abstract class SampleTestDataGenerator {

	public static SampleObservationDto createRandomSampleObservation() {
		final SampleObservationDto sampleObservationDto =
			new SampleObservationDto(Integer.valueOf(randomNumeric(6)), randomAlphanumeric(6), randomAlphanumeric(6),
				randomAlphanumeric(6));
		sampleObservationDto.setTakenBy(randomAlphanumeric(6));
		sampleObservationDto.setSampleDate(randomAlphanumeric(10));
		sampleObservationDto.setSampleType(randomAlphanumeric(6));
		sampleObservationDto.setTissueType(randomAlphanumeric(6));
		sampleObservationDto.setNotes(randomAlphanumeric(6));
		sampleObservationDto.setStudyName(randomAlphanumeric(6));
		sampleObservationDto.setSeason(randomAlphanumeric(6));
		sampleObservationDto.setLocationName(randomAlphanumeric(6));
		sampleObservationDto.setEntryNumber(Integer.valueOf(randomNumeric(6)));
		sampleObservationDto.setPlotNumber(Integer.valueOf(randomNumeric(6)));

		sampleObservationDto.setGermplasmDbId(randomAlphanumeric(6));
		sampleObservationDto.setPlantingDate(randomAlphanumeric(6));
		sampleObservationDto.setHarvestDate(randomAlphanumeric(6));
		return sampleObservationDto;
	}

	public static SampleDetailsDTO createRandomSampleDetails() {
		final SampleDetailsDTO sampleDetailsDTO =
			new SampleDetailsDTO(Integer.valueOf(randomNumeric(6)), randomAlphanumeric(6), randomAlphanumeric(6));
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
		sampleDetailsDTO.setGermplasmUUID(randomAlphanumeric(6));
		sampleDetailsDTO.setSeedingDate(randomAlphanumeric(6));
		sampleDetailsDTO.setHarvestDate(randomAlphanumeric(6));
		return sampleDetailsDTO;
	}
}
