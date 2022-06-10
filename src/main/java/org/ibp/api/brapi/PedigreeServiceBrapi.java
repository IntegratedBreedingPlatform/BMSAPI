package org.ibp.api.brapi;

import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeDTO;
import org.ibp.api.brapi.v2.germplasm.PedigreeNodesUpdateResponse;

import java.util.List;
import java.util.Map;

public interface PedigreeServiceBrapi {

	PedigreeNodesUpdateResponse updatePedigreeNodes(Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap);

}
