package org.ibp.api.brapi.v2.variable;

import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.brapi.v2.BrapiUpdateResponse;

public class VariableUpdateResponse extends BrapiUpdateResponse<VariableDTO> {

	@Override
	public String getEntity() {
		return "entity.variable";
	}

	@Override
	public String getEntityName() {
		return this.getEntityObject().getObservationVariableName();
	}
}
