package org.ibp.api.brapi.v2.observation;

import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.ibp.api.brapi.v2.BrapiImportResponse;

public class ObservationImportResponse extends BrapiImportResponse<ObservationDto> {

	@Override
	public String getEntity() {
		return "entity.observation";
	}

}
