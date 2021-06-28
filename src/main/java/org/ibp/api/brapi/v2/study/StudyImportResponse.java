package org.ibp.api.brapi.v2.study;

import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.ibp.api.brapi.v2.BrapiImportResponse;

public class StudyImportResponse extends BrapiImportResponse<StudyInstanceDto> {

	@Override
	public String getEntity() {
		return "entity.study";
	}
}
