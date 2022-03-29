package org.ibp.api.brapi.v2.list;

import org.generationcp.middleware.service.api.GermplasmListDTO;
import org.ibp.api.brapi.v2.BrapiImportResponse;

public class GermplasmListImportResponse  extends BrapiImportResponse<GermplasmListDTO> {

	@Override
	public String getEntity() {
		return "entity.list";
	}
}
