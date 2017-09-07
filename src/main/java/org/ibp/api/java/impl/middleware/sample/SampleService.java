package org.ibp.api.java.impl.middleware.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.ibp.api.domain.sample.SampleObservationDto;

import java.util.List;

public interface SampleService {
	List<SampleDTO> getSamples(final String plotId);

	SampleObservationDto getSampleObservation(final String sampleId);
}
