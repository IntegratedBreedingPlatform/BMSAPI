package org.ibp.api.brapi.v2.trial;

import org.generationcp.middleware.domain.dms.StudySummary;
import org.ibp.api.brapi.v2.BrapiImportResponse;

public class TrialImportResponse extends BrapiImportResponse<StudySummary> {

	@Override
	public String getEntity() {
		return "entity.trial";
	}
}
