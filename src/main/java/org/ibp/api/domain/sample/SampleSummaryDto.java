package org.ibp.api.domain.sample;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.generationcp.middleware.service.api.sample.SampleObservationDto;
import org.ibp.api.brapi.v1.common.Metadata;
import org.pojomatic.Pojomatic;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"metadata", "result"})
public class SampleSummaryDto {

	private Metadata metadata;

	private SampleObservationDto result;

	/**
	 * @return Metadata
	 */
	public Metadata getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata The metadata
	 * @return this
	 */
	public SampleSummaryDto setMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	/**
	 * @return The result
	 */
	public SampleObservationDto getResult() {
		return result;
	}

	/**
	 * @param result The result
	 * @return this
	 */
	public SampleSummaryDto setResult(final SampleObservationDto result) {
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
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
