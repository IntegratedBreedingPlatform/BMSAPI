
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
@JsonPropertyOrder({"pagination", "status"})
public class Metadata {

	@JsonProperty("pagination")
	private Pagination pagination;
	@JsonProperty("status")
	private List<Object> status = new ArrayList<Object>();
	@JsonIgnore
	private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Metadata() {
	}

	/**
	 *
	 * @param status
	 * @param pagination
	 */
	public Metadata(final Pagination pagination, final List<Object> status) {
		this.pagination = pagination;
		this.status = status;
	}

	/**
	 *
	 * @return The pagination
	 */
	@JsonProperty("pagination")
	public Pagination getPagination() {
		return this.pagination;
	}

	/**
	 *
	 * @param pagination The pagination
	 */
	@JsonProperty("pagination")
	public void setPagination(final Pagination pagination) {
		this.pagination = pagination;
	}

	public Metadata withPagination(final Pagination pagination) {
		this.pagination = pagination;
		return this;
	}

	/**
	 *
	 * @return The status
	 */
	@JsonProperty("status")
	public List<Object> getStatus() {
		return this.status;
	}

	/**
	 *
	 * @param status The status
	 */
	@JsonProperty("status")
	public void setStatus(final List<Object> status) {
		this.status = status;
	}

	public Metadata withStatus(final List<Object> status) {
		this.status = status;
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

	public Metadata withAdditionalProperty(final String name, final Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

}
