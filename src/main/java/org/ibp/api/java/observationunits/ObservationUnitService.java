package org.ibp.api.java.observationunits;

import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportRequestDto;
import org.ibp.api.brapi.v2.observationunits.ObservationUnitImportResponse;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;

import java.util.List;

public interface ObservationUnitService {

	ObservationUnitImportResponse createObservationUnits(String cropName,
		List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos);

	List<ObservationUnitDto> searchObservationUnits(Integer pageSize, Integer pageNumber, ObservationUnitSearchRequestDTO requestDTO);

	long countObservationUnits(ObservationUnitSearchRequestDTO requestDTO);

}
