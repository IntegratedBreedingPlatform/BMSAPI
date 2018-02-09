package org.ibp.api.brapi.v1.phenotype;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchDTO;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Result;
import org.pojomatic.Pojomatic;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "metadata", "result" })
public class PhenotypeSearchSummariesDto {

	private Metadata metadata;
	private Result<PhenotypeSearchDTO> result;

	public PhenotypeSearchSummariesDto() {
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public PhenotypeSearchSummariesDto setMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	public Result<PhenotypeSearchDTO> getResult() {
		return result;
	}

	public PhenotypeSearchSummariesDto setResult(final Result<PhenotypeSearchDTO> result) {
		this.result = result;
		return this;
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
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

}
