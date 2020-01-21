package org.ibp.api.brapi.v1.germplasm;

import org.generationcp.middleware.domain.germplasm.AttributeDTO;

import java.util.List;

public class GermplasmAttributes {

	private List<AttributeDTO> data;

	private String germplasmDbId;

	public List<AttributeDTO> getData() {
		return this.data;
	}

	public void setData(final List<AttributeDTO> data) {
		this.data = data;
	}

	public String getGermplasmDbId() {
		return this.germplasmDbId;
	}

	public void setGermplasmDbId(final String germplasmDbId) {
		this.germplasmDbId = germplasmDbId;
	}
}
