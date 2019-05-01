package org.ibp.api.java.impl.middleware.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.service.api.sample.SampleSearchRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SampleService {

	List<SampleDTO> filter(final String obsUnitId, final Integer listId, final Pageable pageable);

	/**
	 * count results from {@link #filter}
	 */
	long countFilter(final String obsUnitId, final Integer listId);

	SampleDetailsDTO getSampleObservation(final String sampleId);

	List<SampleDetailsDTO> searchSamples(final SampleSearchRequestDto sampleSearchRequestDto);

	long countSearchSamples(SampleSearchRequestDto sampleSearchRequestDto);

}
