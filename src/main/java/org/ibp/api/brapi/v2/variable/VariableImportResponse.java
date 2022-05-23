package org.ibp.api.brapi.v2.variable;

import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.brapi.v2.BrapiImportResponse;

public class VariableImportResponse extends BrapiImportResponse<VariableDTO> {

	@Override
	public String getEntity() {
		return "entity.variable";
	}

}
