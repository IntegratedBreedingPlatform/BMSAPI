package org.ibp.api.brapi.v1.germplasm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.ibp.api.brapi.v1.common.Metadata;
import org.pojomatic.Pojomatic;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"metadata", "result"})
public class SingleGermplasmResponse {

	private Metadata metadata;

	private Germplasm result;

	public SingleGermplasmResponse() {
	}

	public SingleGermplasmResponse(final Metadata metadata, final Germplasm result) {
		this.metadata = metadata;
		this.result = result;
	}

	public SingleGermplasmResponse withMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(final Metadata metadata) {
		this.metadata = metadata;
	}

	public Germplasm getResult() {
		return result;
	}

	public void setResult(final Germplasm result) {
		this.result = result;
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
