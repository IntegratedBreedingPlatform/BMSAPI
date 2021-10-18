package org.ibp.api.java.impl.middleware.observation.brapi;

import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationSearchRequestDto;
import org.ibp.api.brapi.ObservationServiceBrapi;
import org.ibp.api.brapi.v2.observation.ObservationImportResponse;
import org.ibp.api.java.impl.middleware.study.validator.ObservationImportRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

@Service
public class ObservationServiceBrapiImpl implements ObservationServiceBrapi {

	@Autowired
	private org.generationcp.middleware.api.brapi.ObservationServiceBrapi observationServiceBrapi;

	@Autowired
	private ObservationImportRequestValidator observationImportRequestValidator;

	@Override
	public List<ObservationDto> searchObservations(final ObservationSearchRequestDto observationSearchRequestDto,
		final Pageable pageable) {
		return this.observationServiceBrapi.searchObservations(observationSearchRequestDto, pageable);
	}

	@Override
	public long countObservations(final ObservationSearchRequestDto observationSearchRequestDto) {
		return this.observationServiceBrapi.countObservations(observationSearchRequestDto);
	}

	@Override
	public ObservationImportResponse createObservations(final List<ObservationDto> observations) {

		final ObservationImportResponse response = new ObservationImportResponse();
		response.setImportListSize(observations.size());
		response.setCreatedSize(0);

		final BindingResult bindingResult = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observations);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}

		if (!CollectionUtils.isEmpty(observations)) {
			final List<ObservationDto> results =
				this.observationServiceBrapi.createObservations(observations);
			response.setEntityList(results);
			response.setCreatedSize(results.size());
		}
		return response;

	}
}
