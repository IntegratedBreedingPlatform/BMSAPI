
package org.ibp.api.brapi.v1.location;

import java.util.HashMap;
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
@JsonPropertyOrder({"pageNumber", "pageSize", "totalCount", "totalPages"})
public class Pagination {

	@JsonProperty("pageNumber")
	private Integer pageNumber;
	@JsonProperty("pageSize")
	private Integer pageSize;
	@JsonProperty("totalCount")
	private Integer totalCount;
	@JsonProperty("totalPages")
	private Integer totalPages;
	@JsonIgnore
	private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Pagination() {
	}

	/**
	 *
	 * @param totalCount
	 * @param pageSize
	 * @param pageNumber
	 * @param totalPages
	 */
	public Pagination(final Integer pageNumber, final Integer pageSize, final Integer totalCount, final Integer totalPages) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.totalCount = totalCount;
		this.totalPages = totalPages;
	}

	/**
	 *
	 * @return The pageNumber
	 */
	@JsonProperty("pageNumber")
	public Integer getPageNumber() {
		return this.pageNumber;
	}

	/**
	 *
	 * @param pageNumber The pageNumber
	 */
	@JsonProperty("pageNumber")
	public void setPageNumber(final Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Pagination withPageNumber(final Integer pageNumber) {
		this.pageNumber = pageNumber;
		return this;
	}

	/**
	 *
	 * @return The pageSize
	 */
	@JsonProperty("pageSize")
	public Integer getPageSize() {
		return this.pageSize;
	}

	/**
	 *
	 * @param pageSize The pageSize
	 */
	@JsonProperty("pageSize")
	public void setPageSize(final Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Pagination withPageSize(final Integer pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	/**
	 *
	 * @return The totalCount
	 */
	@JsonProperty("totalCount")
	public Integer getTotalCount() {
		return this.totalCount;
	}

	/**
	 *
	 * @param totalCount The totalCount
	 */
	@JsonProperty("totalCount")
	public void setTotalCount(final Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Pagination withTotalCount(final Integer totalCount) {
		this.totalCount = totalCount;
		return this;
	}

	/**
	 *
	 * @return The totalPages
	 */
	@JsonProperty("totalPages")
	public Integer getTotalPages() {
		return this.totalPages;
	}

	/**
	 *
	 * @param totalPages The totalPages
	 */
	@JsonProperty("totalPages")
	public void setTotalPages(final Integer totalPages) {
		this.totalPages = totalPages;
	}

	public Pagination withTotalPages(final Integer totalPages) {
		this.totalPages = totalPages;
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

	public Pagination withAdditionalProperty(final String name, final Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

}
