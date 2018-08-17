package org.ibp.api.brapi.v1.germplasm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.pojomatic.Pojomatic;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"sourceName", "taxonId",})
class Taxon {

	private String sourceName;

	private String taxonId;

	public Taxon() {
	}

	public Taxon(final String sourceName, final String taxonId) {
		this.sourceName = sourceName;
		this.taxonId = taxonId;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(final String sourceName) {
		this.sourceName = sourceName;
	}

	public String getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(final String taxonId) {
		this.taxonId = taxonId;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
