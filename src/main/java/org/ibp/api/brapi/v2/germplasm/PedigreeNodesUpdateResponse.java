package org.ibp.api.brapi.v2.germplasm;

import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeDTO;
import org.ibp.api.brapi.v2.BrapiBatchUpdateResponse;

public class PedigreeNodesUpdateResponse extends BrapiBatchUpdateResponse<PedigreeNodeDTO> {

	@Override
	public String getEntity() {
		return "entity.pedigreenode";
	}

}
