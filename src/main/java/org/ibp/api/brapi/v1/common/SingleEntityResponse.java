package org.ibp.api.brapi.v1.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableMap;
import org.pojomatic.Pojomatic;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"metadata", "result"})
public class SingleEntityResponse<T> {

	private Metadata metadata;

	private T result;

	public SingleEntityResponse() {
		this.metadata = new Metadata();
	}

	public SingleEntityResponse(final T result) {
		this();
		this.result = result;
	}

	public SingleEntityResponse(final Metadata metadata, final T result) {
		this.metadata = metadata;
		this.result = result;
	}

	public SingleEntityResponse withMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(final Metadata metadata) {
		this.metadata = metadata;
	}

	public T getResult() {
		return result;
	}

	public void setResult(final T result) {
		this.result = result;
	}

	public SingleEntityResponse<T> withMessage(final String message) {
		this.metadata.getStatus().add(ImmutableMap.of("message",  message));
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
