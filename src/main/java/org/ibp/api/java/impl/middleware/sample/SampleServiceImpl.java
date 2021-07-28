package org.ibp.api.java.impl.middleware.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.SampleSearchRequestDTO;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.sample.SampleObservationDto;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class SampleServiceImpl implements SampleService {

	@Autowired
	org.generationcp.middleware.service.api.SampleService sampleService;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Override
	public List<SampleDTO> filter(final String obsUnitId, final Integer listId, final Pageable pageable) {
		return this.sampleService.filter(obsUnitId, listId, pageable);
	}

	@Override
	public long countFilter(final String obsUnitId, final Integer listId) {
		return this.sampleService.countFilter(obsUnitId, listId);
	}

	@Override
	public SampleDetailsDTO getSampleObservation(final String sampleId){
		final SampleDetailsDTO sampleDetailsDTO;
		try {
			sampleDetailsDTO = this.sampleService.getSampleObservation(sampleId);
		} catch (final MiddlewareException e) {
			throw new ApiRuntimeException("an error happened when try to get the sample", e);
		}
		return sampleDetailsDTO;
	}

	@Override
	public List<SampleDTO> getGermplasmSamples(final Integer gid) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		return this.sampleService.getByGid(gid);
	}

	@Override
	public List<SampleObservationDto> getSampleObservations(final SampleSearchRequestDTO requestDTO, final Pageable pageable) {
		return this.sampleService.getSampleObservations(requestDTO, pageable);
	}

	@Override
	public long countSampleObservations(final SampleSearchRequestDTO sampleSearchRequestDTO) {
		return this.sampleService.countSampleObservations(sampleSearchRequestDTO);
	}
}
