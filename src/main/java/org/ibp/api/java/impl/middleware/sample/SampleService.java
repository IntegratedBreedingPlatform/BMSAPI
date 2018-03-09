package org.ibp.api.java.impl.middleware.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;

import java.util.List;

public interface SampleService {
	List<SampleDTO> filter(final String plotId, Integer listId);

	SampleDetailsDTO getSampleObservation(final String sampleId);
}
