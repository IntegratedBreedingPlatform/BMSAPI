package org.ibp.api.brapi;

import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationSearchRequestDto;
import org.ibp.api.brapi.v2.observation.ObservationImportResponse;
import org.ibp.api.brapi.v2.observation.ObservationUpdateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ObservationServiceBrapi {

	List<ObservationDto> searchObservations(ObservationSearchRequestDto observationSearchRequestDto, Pageable pageable);

	long countObservations(ObservationSearchRequestDto observationSearchRequestDto);

	ObservationImportResponse createObservations(List<ObservationDto> observations);

	ObservationUpdateResponse updateObservations(Map<String, ObservationDto> observations);
}
