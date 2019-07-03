package org.ibp.api.brapi.v1.observation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Result;
import org.pojomatic.Pojomatic;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "metadata", "result" })
public class ObservationLevels {

	private Metadata metadata;
	private Result<String> result;

	public ObservationLevels() {
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public ObservationLevels setMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	public Result<String> getResult() {
		return result;
	}

	public ObservationLevels setResult(final Result<String> result) {
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
