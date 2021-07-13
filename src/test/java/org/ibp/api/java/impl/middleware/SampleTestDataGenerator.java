package org.ibp.api.java.impl.middleware;

import org.generationcp.middleware.domain.sample.SampleDetailsDTO;

import java.util.Date;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

public abstract class SampleTestDataGenerator {

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
		sampleDetailsDTO.setPlateId(randomAlphanumeric(6));
		sampleDetailsDTO.setSampleNumber(Integer.valueOf(randomNumeric(6)));
		return sampleDetailsDTO;
	}
}
