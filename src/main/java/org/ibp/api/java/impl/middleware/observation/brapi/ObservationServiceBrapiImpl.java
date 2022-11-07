package org.ibp.api.java.impl.middleware.observation.brapi;

import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationSearchRequestDto;
import org.ibp.api.brapi.ObservationServiceBrapi;
import org.ibp.api.brapi.v2.observation.ObservationImportResponse;
import org.ibp.api.brapi.v2.observation.ObservationUpdateResponse;
import org.ibp.api.java.impl.middleware.study.validator.ObservationImportRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	@Override
	public ObservationUpdateResponse updateObservations(final Map<String, ObservationDto> observations) {
		// Set key to observationDbId, so we can just work with List instead of Map
		observations.keySet().forEach(key -> observations.get(key).setObservationDbId(key));

		final ObservationUpdateResponse response = new ObservationUpdateResponse();
		response.setUpdateListSize(observations.size());
		response.setUpdatedSize(0);

		final List<ObservationDto> observationDtos = observations.values().stream().collect(Collectors.toList());
		final BindingResult bindingResult = this.observationImportRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}

		if (!CollectionUtils.isEmpty(observationDtos)) {
			final List<ObservationDto> results =
					this.observationServiceBrapi.updateObservations(observationDtos);
			response.setEntityList(results);
			response.setUpdatedSize(results.size());
		}
		return response;
	}
}
