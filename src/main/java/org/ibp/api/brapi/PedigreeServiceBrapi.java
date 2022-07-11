package org.ibp.api.brapi;

import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeSearchRequest;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmSearchRequest;
import org.ibp.api.brapi.v2.germplasm.PedigreeNodesUpdateResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PedigreeServiceBrapi {

	PedigreeNodesUpdateResponse updatePedigreeNodes(Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap);

	List<PedigreeNodeDTO> searchPedigreeNodes(PedigreeNodeSearchRequest pedigreeNodeSearchRequest, Pageable pageable);

	long countPedigreeNodes(PedigreeNodeSearchRequest pedigreeNodeSearchRequest);

}
