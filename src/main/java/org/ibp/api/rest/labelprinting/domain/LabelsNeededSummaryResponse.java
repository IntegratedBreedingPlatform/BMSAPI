package org.ibp.api.rest.labelprinting.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;
import java.util.Map;

@AutoProperty
public class LabelsNeededSummaryResponse {

	private List<String> headers;

	private List<Map<String, String>> values;

	private Long totalNumberOfLabelsNeeded;

	public LabelsNeededSummaryResponse(
		final List<String> headers, final List<Map<String, String>> values, final Long totalNumberOfLabelsNeeded) {
		this.headers = headers;
		this.values = values;
		this.totalNumberOfLabelsNeeded = totalNumberOfLabelsNeeded;
	}

	public List<String> getHeaders() {
		return this.headers;
	}

	public void setHeaders(final List<String> headers) {
		this.headers = headers;
	}

	public List<Map<String, String>> getValues() {
		return this.values;
	}

	public void setValues(final List<Map<String, String>> values) {
		this.values = values;
	}

	public Long getTotalNumberOfLabelsNeeded() {
		return this.totalNumberOfLabelsNeeded;
	}

	public void setTotalNumberOfLabelsNeeded(final Long totalNumberOfLabelsNeeded) {
		this.totalNumberOfLabelsNeeded = totalNumberOfLabelsNeeded;
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
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

}
