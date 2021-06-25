package org.ibp.api.java.observationunits;

import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportRequestDto;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportResponse;

import java.util.List;

public interface ObservationUnitService {

	ObservationUnitImportResponse createObservationUnits(String cropName,
		List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos);

}
