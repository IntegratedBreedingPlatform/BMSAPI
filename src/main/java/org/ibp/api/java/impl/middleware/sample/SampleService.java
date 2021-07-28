package org.ibp.api.java.impl.middleware.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.SampleSearchRequestDTO;
import org.generationcp.middleware.service.api.sample.SampleObservationDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SampleService {

	List<SampleDTO> filter(final String obsUnitId, final Integer listId, final Pageable pageable);

	/**
	 * count results from {@link #filter}
	 */
	long countFilter(final String obsUnitId, final Integer listId);

	SampleDetailsDTO getSampleObservation(final String sampleId);

	List<SampleDTO> getGermplasmSamples(Integer gid);

	List<SampleObservationDto> getSampleObservations(SampleSearchRequestDTO sampleSearchRequestDTO, Pageable pageable);

	long countSampleObservations(SampleSearchRequestDTO sampleSearchRequestDTO);
}
