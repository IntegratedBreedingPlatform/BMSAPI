
package org.ibp.api.brapi.v1.common;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"pagination", "status", "datafiles"})
public class Metadata {

	private Pagination pagination;

	private Map<String, String> status;

	private URL[] datafiles;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Metadata() {
		this.pagination = new Pagination(0, 0, 0L, 0);
		this.status = new HashMap<>();
		this.datafiles = new URL[] {};
	}

	/**
	 *
	 * @param status
	 * @param pagination
	 */
	public Metadata(final Pagination pagination, final Map<String, String> status) {
		this.pagination = pagination;
		this.status = status;
	}

	/**
	 *
	 * @param pagination
	 * @param status
	 * @param datafiles
	 */
	public Metadata(final Pagination pagination, final Map<String, String> status, URL[] datafiles) {
		this.pagination = pagination;
		this.status = status;
		this.datafiles = datafiles;
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
	public Map<String, String> getStatus() {
		return this.status;
	}

	/**
	 *
	 * @param status The status
	 */
	public void setStatus(final Map<String, String> status) {
		this.status = status;
	}

	public Metadata withStatus(final Map<String, String> status) {
		this.status = status;
		return this;
	}

	/**
	 * @return the datafiles
	 */
	public URL[] getDatafiles() {
		return datafiles;
	}

	/**
	 * @param datafiles the datafiles to set
	 */
	public void setDatafiles(URL[] datafiles) {
		this.datafiles = datafiles;
	}

	/**
	 * @return the datafiles
	 */
	public Metadata withDatafiles(URL[] datafiles) {
		this.datafiles = datafiles;
		return this;
	}
}
