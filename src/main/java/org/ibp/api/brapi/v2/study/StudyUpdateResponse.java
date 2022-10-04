package org.ibp.api.brapi.v2.study;

import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.ibp.api.brapi.v2.BrapiUpdateResponse;

public class StudyUpdateResponse extends BrapiUpdateResponse<StudyInstanceDto> {

	private String entityName;

	@Override
	public String getEntity() {
		return "entity.study";
	}

	@Override
	public String getEntityName() {
		return this.entityName;
	}

	public void setEntityName(final String entityName) {
		this.entityName = entityName;
	}
}
