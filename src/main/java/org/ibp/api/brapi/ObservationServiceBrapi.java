package org.ibp.api.brapi;

import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationSearchRequestDto;
import org.ibp.api.brapi.v2.observation.ObservationImportResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ObservationServiceBrapi {

	List<ObservationDto> searchObservations(ObservationSearchRequestDto observationSearchRequestDto, Integer pageSize, Integer pageNumber);

	long countObservations(ObservationSearchRequestDto observationSearchRequestDto);

	ObservationImportResponse createObservations(List<ObservationDto> observations);
}
