package org.ibp.api.brapi.v2.germplasm;

import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.ibp.api.brapi.v2.BrapiImportResponse;

public class GermplasmImportResponse extends BrapiImportResponse<GermplasmDTO> {

	@Override
	public String getEntity() {
		return "entity.germplasm";
	}
}
