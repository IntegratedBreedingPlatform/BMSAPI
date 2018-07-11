
package org.ibp.api.brapi.v1.calls;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Result;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"metadata", "result"})
public class BrapiCalls {

	private Metadata metadata;

	private Result<BrapiCall> result;

	/**
	 * No args constructor required by serialization libraries.
	 */
	public BrapiCalls() {
	}

	/**
	 * @param result
	 * @param metadata
	 */
	public BrapiCalls(final Metadata metadata, final Result<BrapiCall> result) {
		this.metadata = metadata;
		this.result = result;
	}

	/**
	 * @return The metadata
	 */
	public Metadata getMetadata() {
		return this.metadata;
	}

	/**
	 * @param metadata The metadata
	 */
	public void setMetadata(final Metadata metadata) {
		this.metadata = metadata;
	}

	public BrapiCalls withMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	/**
	 * @return The result
	 */
	public Result<BrapiCall> getResult() {
		return this.result;
	}

	/**
	 * @param result The result
	 */
	public void setResult(final Result<BrapiCall> result) {
		this.result = result;
	}

	public BrapiCalls withResult(final Result<BrapiCall> result) {
		this.result = result;
		return this;
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
}
