package org.ibp.api.rest.labelprinting;

import java.util.List;
import java.util.Map;

public class LabelsNeededSummary {

	private List<String> headers;

	private List<Map<String, String>> values;

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(final List<String> headers) {
		this.headers = headers;
	}

	public List<Map<String, String>> getValues() {
		return values;
	}

	public void setValues(final List<Map<String, String>> values) {
		this.values = values;
	}

}
