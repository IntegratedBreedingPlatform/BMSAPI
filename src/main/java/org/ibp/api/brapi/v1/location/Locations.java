
package org.ibp.api.brapi.v1.location;

import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"metadata", "result"})
public class Locations {

	@JsonProperty("metadata")
	private Metadata metadata;

	@JsonProperty("result")
	private Result<Location> result;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Locations() {
	}

	/**
	 *
	 * @param result
	 * @param metadata
	 */
	public Locations(final Metadata metadata, final Result<Location> result) {
		this.metadata = metadata;
		this.result = result;
	}

	/**
	 *
	 * @return The metadata
	 */
	@JsonProperty("metadata")
	public Metadata getMetadata() {
		return this.metadata;
	}

	/**
	 *
	 * @param metadata The metadata
	 */
	@JsonProperty("metadata")
	public void setMetadata(final Metadata metadata) {
		this.metadata = metadata;
	}

	public Locations withMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	/**
	 *
	 * @return The result
	 */
	@JsonProperty("result")
	public Result<Location> getResult() {
		return this.result;
	}

	/**
	 *
	 * @param result The result
	 */
	@JsonProperty("result")
	public void setResult(final Result<Location> result) {
		this.result = result;
	}

	public Locations withResult(final Result<Location> result) {
		this.result = result;
		return this;
	}
}
