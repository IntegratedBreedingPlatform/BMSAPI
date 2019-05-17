package org.ibp.api.brapi.v1.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableMap;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"metadata", "result"})
public class EntityListResponse<T> {

	private Metadata metadata;

	private Result<T> result;

	public EntityListResponse() {
		this.metadata = new Metadata();
	}

	public EntityListResponse(final Result<T> result) {
		this();
		this.result = result;
	}

	public EntityListResponse(final Metadata metadata, final Result<T> result) {
		this.metadata = metadata;
		this.result = result;
	}

	public EntityListResponse withMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(final Metadata metadata) {
		this.metadata = metadata;
	}

	public Result<T> getResult() {
		return result;
	}

	public void setResult(final Result<T> result) {
		this.result = result;
	}

	public EntityListResponse<T> withMessage(final String message) {
		this.metadata.getStatus().add(ImmutableMap.of("message", message));
		return this;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}
}
