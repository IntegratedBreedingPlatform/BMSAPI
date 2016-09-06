
package org.ibp.api.brapi.v1.common;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"pagination", "status"})
public class Metadata {

	private Pagination pagination;

	private List<Object> status = new ArrayList<Object>();

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
	public Pagination getPagination() {
		return this.pagination;
	}

	/**
	 *
	 * @param pagination The pagination
	 */
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
	public List<Object> getStatus() {
		return this.status;
	}

	/**
	 *
	 * @param status The status
	 */
	public void setStatus(final List<Object> status) {
		this.status = status;
	}

	public Metadata withStatus(final List<Object> status) {
		this.status = status;
		return this;
	}
}
