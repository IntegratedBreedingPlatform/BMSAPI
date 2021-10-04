package org.ibp.api.java.impl.middleware.ontology.brapi;

import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.brapi.VariableServiceBrapi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VariableServiceBrapiImpl implements VariableServiceBrapi {

    @Autowired
    private org.generationcp.middleware.api.brapi.VariableServiceBrapi middlewareVariableServiceBrapi;

    public List<VariableDTO> getObservationVariables(final String crop, final VariableSearchRequestDTO requestDTO, final Pageable pageable) {
        return this.middlewareVariableServiceBrapi.getObservationVariables(crop, requestDTO, pageable);
    }

    public long countObservationVariables(final VariableSearchRequestDTO requestDTO){
        return this.middlewareVariableServiceBrapi.countObservationVariables(requestDTO);
    }
}
