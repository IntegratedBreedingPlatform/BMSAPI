package org.ibp.api.brapi.v2.trial;

import org.generationcp.middleware.domain.dms.TrialSummary;
import org.ibp.api.brapi.v2.BrapiImportResponse;

public class TrialImportResponse extends BrapiImportResponse<TrialSummary> {

	@Override
	public String getEntity() {
		return "entity.trial";
	}
}
