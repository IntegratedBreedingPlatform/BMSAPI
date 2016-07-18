
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

/**
 * 
 * Generic result object to be used for BrAPI responses where "result" is a collection of type T.
 *
 * @param <T> the type of the object that the "data" collection holds.
 * 
 * @author Naymesh Mistry
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"data"})
public class Result<T> {

	@JsonProperty("data")
	private List<T> data = new ArrayList<T>();
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
	public Result(final List<T> data) {
		this.data = data;
	}

	/**
	 *
	 * @return The data
	 */
	@JsonProperty("data")
	public List<T> getData() {
		return this.data;
	}

	/**
	 *
	 * @param data The data
	 */
	@JsonProperty("data")
	public void setData(final List<T> data) {
		this.data = data;
	}

	public Result<T> withData(final List<T> data) {
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

	public Result<T> withAdditionalProperty(final String name, final Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

}
