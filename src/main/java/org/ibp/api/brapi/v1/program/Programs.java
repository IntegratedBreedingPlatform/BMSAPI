package org.ibp.api.brapi.v1.program;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Result;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"metadata", "result"})
public class Programs {

	private Metadata metadata;

	private Result<Program> result;

	/**
	 * No args constructor required by serialization libraries.
	 */
	public Programs() {
	}

	/**
	 * @param result
	 * @param metadata
	 */
	public Programs(final Metadata metadata, final Result<Program> result) {
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

	public Programs withMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	/**
	 * @return The result
	 */
	public Result<Program> getResult() {
		return this.result;
	}

	/**
	 * @param result The result
	 */
	public void setResult(final Result<Program> result) {
		this.result = result;
	}

	public Programs withResult(final Result<Program> result) {
		this.result = result;
		return this;
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
}
