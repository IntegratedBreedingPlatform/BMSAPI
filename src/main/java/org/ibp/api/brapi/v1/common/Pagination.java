
package org.ibp.api.brapi.v1.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"pageNumber", "pageSize", "totalCount", "totalPages"})
public class Pagination {

	@JsonProperty("pageNumber")
	private Integer pageNumber;

	@JsonProperty("pageSize")
	private Integer pageSize;

	@JsonProperty("totalCount")
	private Long totalCount;

	@JsonProperty("totalPages")
	private Integer totalPages;

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
	public Pagination(final Integer pageNumber, final Integer pageSize, final Long totalCount, final Integer totalPages) {
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
	public Long getTotalCount() {
		return this.totalCount;
	}

	/**
	 *
	 * @param totalCount The totalCount
	 */
	@JsonProperty("totalCount")
	public void setTotalCount(final Long totalCount) {
		this.totalCount = totalCount;
	}

	public Pagination withTotalCount(final Long totalCount) {
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
}
