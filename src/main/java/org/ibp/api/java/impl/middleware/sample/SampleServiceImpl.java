package org.ibp.api.java.impl.middleware.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.exception.ApiRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SampleServiceImpl implements SampleService {

	@Autowired
	org.generationcp.middleware.service.api.SampleService sampleService;

	@Override
	public List<SampleDTO> filter(final String plotId, final Integer listId, final Pageable pageable) {
		return this.sampleService.filter(plotId, listId, pageable);
	}

	@Override
	public long countFilter(final String plotId, final Integer listId) {
		return this.sampleService.countFilter(plotId, listId);
	}

	@Override
	public SampleDetailsDTO getSampleObservation(final String sampleId){
		final SampleDetailsDTO sampleDetailsDTO;
		try {
			sampleDetailsDTO = this.sampleService.getSampleObservation(sampleId);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("an error happened when try to get the sample", e);
		}
		return sampleDetailsDTO;
	}
}
