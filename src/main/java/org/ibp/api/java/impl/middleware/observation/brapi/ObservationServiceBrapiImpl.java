package org.ibp.api.java.impl.middleware.observation.brapi;

import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationSearchRequestDto;
import org.ibp.api.brapi.ObservationServiceBrapi;
import org.ibp.api.brapi.v2.observation.ObservationImportResponse;
import org.ibp.api.java.impl.middleware.study.validator.ObservationImportRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
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
		final Integer pageSize, final Integer pageNumber) {
		return this.observationServiceBrapi.searchObservations(observationSearchRequestDto, pageSize, pageNumber);
	}

	@Override
	public long countObservations(final ObservationSearchRequestDto observationSearchRequestDto) {
		return this.observationServiceBrapi.countObservations(observationSearchRequestDto);
	}

	@Override
	public ObservationImportResponse createObservations(final List<ObservationDto> observations) {

		final ObservationImportResponse response = new ObservationImportResponse();
		final int originalListSize = observations.size();
		final List<ObservationDto> observationDtos = new ArrayList<>();
		int noOfCreatedObservations = 0;


		final BindingResult bindingResult = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observations);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}

		if (!CollectionUtils.isEmpty(observations)) {
			final List<Integer> observationDbIds =
				this.observationServiceBrapi.createObservations(observations);
			if (!CollectionUtils.isEmpty(observationDbIds)) {
				noOfCreatedObservations = observationDbIds.size();
				final ObservationSearchRequestDto searchRequestDTO = new ObservationSearchRequestDto();
				searchRequestDTO.setObservationDbIds(observationDbIds);
				observationDtos.addAll(this.observationServiceBrapi.searchObservations(searchRequestDTO, null, null));

			}
			response.setEntityList(observationDtos);
		}

		response.setImportListSize(originalListSize);
		response.setCreatedSize(noOfCreatedObservations);

		return response;

	}
}
