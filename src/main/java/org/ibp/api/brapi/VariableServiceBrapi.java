package org.ibp.api.brapi;

import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VariableServiceBrapi {

    List<VariableDTO> getObservationVariables(String crop, VariableSearchRequestDTO requestDTO, Pageable pageable);

    long countObservationVariables(VariableSearchRequestDTO requestDTO);
}