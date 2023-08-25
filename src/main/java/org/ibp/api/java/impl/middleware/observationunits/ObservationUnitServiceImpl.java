package org.ibp.api.java.impl.middleware.observationunits;

import org.generationcp.middleware.api.brapi.v2.observationlevel.ObservationLevel;
import org.generationcp.middleware.api.brapi.v2.observationlevel.ObservationLevelFilter;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportRequestDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.ibp.api.brapi.v2.observationunits.ObservationUnitImportResponse;
import org.ibp.api.java.observationunits.ObservationUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ObservationUnitServiceImpl implements ObservationUnitService {

	@Autowired
	private ObservationUnitImportRequestValidator observationUnitImportRequestValidator;

	@Autowired
	private org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService middlewareObservationUnitService;

	@Autowired
	private ObservationLevelFilterValidator observationLevelFilterValidator;

	@Override
	public ObservationUnitImportResponse createObservationUnits(final String cropName,
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos) {
		final ObservationUnitImportResponse response = new ObservationUnitImportResponse();
		final int originalListSize = observationUnitImportRequestDtos.size();
		int noOfCreatedObservationUnits = 0;

		// Remove observation units that fails any validation. They will be excluded from creation
		final BindingResult bindingResult =
			this.observationUnitImportRequestValidator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}

		if (!CollectionUtils.isEmpty(observationUnitImportRequestDtos)) {
			final List<String> observationUnitDbIds =
				this.middlewareObservationUnitService.importObservationUnits(cropName, observationUnitImportRequestDtos);
			List<ObservationUnitDto> observationUnitDtos = new ArrayList<>();
			if (!CollectionUtils.isEmpty(observationUnitDbIds)) {
				noOfCreatedObservationUnits = observationUnitDbIds.size();
				final ObservationUnitSearchRequestDTO searchRequestDTO = new ObservationUnitSearchRequestDTO();
				searchRequestDTO.setObservationUnitDbIds(observationUnitDbIds);
				observationUnitDtos = this.middlewareObservationUnitService.searchObservationUnits(null, null, searchRequestDTO);

			}
			response.setEntityList(observationUnitDtos);
		}

		response.setImportListSize(originalListSize);
		response.setCreatedSize(noOfCreatedObservationUnits);
		return response;
	}

	@Override
	public List<ObservationUnitDto> searchObservationUnits(final Integer pageSize, final Integer pageNumber,
		final ObservationUnitSearchRequestDTO requestDTO) {
		return this.middlewareObservationUnitService.searchObservationUnits(pageSize, pageNumber, requestDTO);
	}

	@Override
	public long countObservationUnits(final ObservationUnitSearchRequestDTO requestDTO) {
		return this.middlewareObservationUnitService.countObservationUnits(requestDTO);
	}

	@Override
	public List<ObservationLevel> getObservationLevels(final ObservationLevelFilter observationLevelFilter, final String crop) {
		this.observationLevelFilterValidator.validate(observationLevelFilter, crop);
		return this.middlewareObservationUnitService.getObservationLevels(observationLevelFilter).stream().map(ol -> {
			ol.setLevelName(ol.getLevelName().toLowerCase());
			return ol;
		}).collect(Collectors.toList());
	}

}
