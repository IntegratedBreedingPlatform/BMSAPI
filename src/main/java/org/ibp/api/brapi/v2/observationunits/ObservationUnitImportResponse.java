package org.ibp.api.brapi.v2.observationunits;

import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.ibp.api.brapi.v2.BrapiImportResponse;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;
import org.springframework.validation.ObjectError;

import java.util.List;

public class ObservationUnitImportResponse extends BrapiImportResponse<ObservationUnitDto> {

	@Override
	public String getEntity() {
		return "entity.observationunit";
	}

}
