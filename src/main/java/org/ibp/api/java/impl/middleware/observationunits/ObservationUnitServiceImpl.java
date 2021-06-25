package org.ibp.api.java.impl.middleware.observationunits;

import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportRequestDto;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportResponse;
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

	@Override
	public ObservationUnitImportResponse createObservationUnits(final String cropName,
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos) {
		final ObservationUnitImportResponse response = new ObservationUnitImportResponse();
		final int originalListSize = observationUnitImportRequestDtos.size();
		int noOfCreatedStudies = 0;

		// Remove observation units that fails any validation. They will be excluded from creation
		final BindingResult bindingResult =
			this.observationUnitImportRequestValidator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}

		if (!CollectionUtils.isEmpty(observationUnitImportRequestDtos)) {
			// add call to method that creates observation units
		}

		response.setStatus(noOfCreatedStudies + " out of " + originalListSize + " observation units created successfully.");
		return response;
	}

}
