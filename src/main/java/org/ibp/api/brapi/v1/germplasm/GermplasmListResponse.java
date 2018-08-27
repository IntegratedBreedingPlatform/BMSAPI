package org.ibp.api.brapi.v1.germplasm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Result;
import org.pojomatic.Pojomatic;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"metadata", "result"})
public class GermplasmListResponse {

	private Metadata metadata;

	private Result<Germplasm> result;

	public GermplasmListResponse() {
	}

	public GermplasmListResponse(final Metadata metadata, final Result<Germplasm> result) {
		this.metadata = metadata;
		this.result = result;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(final Metadata metadata) {
		this.metadata = metadata;
	}

	public Result<Germplasm> getResult() {
		return result;
	}

	public void setResult(final Result<Germplasm> result) {
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
