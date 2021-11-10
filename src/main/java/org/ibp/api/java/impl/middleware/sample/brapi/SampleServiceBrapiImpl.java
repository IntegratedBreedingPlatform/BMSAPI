package org.ibp.api.java.impl.middleware.sample.brapi;

import org.generationcp.middleware.domain.search_request.brapi.v2.SampleSearchRequestDTO;
import org.generationcp.middleware.service.api.sample.SampleObservationDto;
import org.ibp.api.brapi.SampleServiceBrapi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SampleServiceBrapiImpl implements SampleServiceBrapi {

	@Autowired
	private org.generationcp.middleware.api.brapi.SampleServiceBrapi sampleServiceBrapi;

	@Override
	public List<SampleObservationDto> getSampleObservations(final SampleSearchRequestDTO requestDTO, final Pageable pageable) {
		return this.sampleServiceBrapi.getSampleObservations(requestDTO, pageable);
	}

	@Override
	public long countSampleObservations(final SampleSearchRequestDTO sampleSearchRequestDTO) {
		return this.sampleServiceBrapi.countSampleObservations(sampleSearchRequestDTO);
	}
}
