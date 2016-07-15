
package org.ibp.api.brapi.v1.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"data"})
public class Result {

	@JsonProperty("data")
	private List<Location> data = new ArrayList<Location>();
	@JsonIgnore
	private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Result() {
	}

	/**
	 *
	 * @param data
	 */
	public Result(final List<Location> data) {
		this.data = data;
	}

	/**
	 *
	 * @return The data
	 */
	@JsonProperty("data")
	public List<Location> getData() {
		return this.data;
	}

	/**
	 *
	 * @param data The data
	 */
	@JsonProperty("data")
	public void setData(final List<Location> data) {
		this.data = data;
	}

	public Result withData(final List<Location> data) {
		this.data = data;
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

	public Result withAdditionalProperty(final String name, final Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

}
