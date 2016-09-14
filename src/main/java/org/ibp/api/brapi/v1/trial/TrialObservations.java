
package org.ibp.api.brapi.v1.trial;

import org.ibp.api.brapi.v1.common.Metadata;
import org.pojomatic.Pojomatic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"metadata", "result"})
public class TrialObservations {

	private Metadata metadata;

	private TrialObservationTable result;

	/**
	 *
	 * @return Metadata
	 */
	public Metadata getMetadata() {
		return this.metadata;
	}

	/**
	 *
	 * @param metadata The metadata
	 * @return this
	 */
	public TrialObservations setMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	/**
	 *
	 * @return The result
	 */
	public TrialObservationTable getResult() {
		return this.result;
	}

	/**
	 *
	 * @param result The result
	 * @return this
	 */
	public TrialObservations setResult(final TrialObservationTable result) {
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
