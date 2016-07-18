
package org.ibp.api.brapi.v1.location;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.ibp.api.brapi.v1.common.Metadata;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"metadata", "result"})
public class Locations {

	@JsonProperty("metadata")
	private Metadata metadata;
	@JsonProperty("result")
	private Result result;
	@JsonIgnore
	private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
	public Locations(final Metadata metadata, final Result result) {
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
	public Result getResult() {
		return this.result;
	}

	/**
	 *
	 * @param result The result
	 */
	@JsonProperty("result")
	public void setResult(final Result result) {
		this.result = result;
	}

	public Locations withResult(final Result result) {
		this.result = result;
		return this;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(final String name, final Object value) {
		this.additionalProperties.put(name, value);
	}

	public Locations withAdditionalProperty(final String name, final Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

}
