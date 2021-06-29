package org.ibp.api.java.impl.middleware.observationunits;

import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportRequestDto;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportResponse;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.observationunits.ObservationUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
@Transactional
public class ObservationUnitServiceImpl implements ObservationUnitService {

	@Autowired
	private ObservationUnitImportRequestValidator observationUnitImportRequestValidator;

	@Autowired
	private org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService middlewareObservationUnitService;

	@Autowired
	private SecurityService securityService;

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
			final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
			final List<ObservationUnitDto> observationUnitDtos =
				this.middlewareObservationUnitService.importObservationUnits(cropName, observationUnitImportRequestDtos, user.getUserid());
			if (!CollectionUtils.isEmpty(observationUnitDtos)) {
				noOfCreatedObservationUnits = observationUnitDtos.size();
			}
			response.setObservationUnits(observationUnitDtos);
		}

		response.setStatus(noOfCreatedObservationUnits + " out of " + originalListSize + " observation units created successfully.");
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

}
