package org.ibp.api.java.impl.middleware.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SampleService {
	List<SampleDTO> filter(final String plotId, final Integer listId, final Pageable pageable);

	SampleDetailsDTO getSampleObservation(final String sampleId);
}
