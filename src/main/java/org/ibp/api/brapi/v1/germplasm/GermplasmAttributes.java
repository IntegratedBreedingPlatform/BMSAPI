package org.ibp.api.brapi.v1.germplasm;

import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
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

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}
}
