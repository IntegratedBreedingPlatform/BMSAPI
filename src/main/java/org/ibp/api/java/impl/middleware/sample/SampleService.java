package org.ibp.api.java.impl.middleware.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;

import java.util.List;

public interface SampleService {
	List<SampleDTO> getSamples(final String plotId);

	SampleDetailsDTO getSampleObservation(final String sampleId);
}
