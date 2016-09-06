
package org.ibp.api.brapi.v1.common;

import java.util.ArrayList;
import java.util.List;

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
@JsonPropertyOrder({"data"})
public class Result<T> {

	@JsonProperty("data")
	private List<T> data = new ArrayList<T>();

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
}
