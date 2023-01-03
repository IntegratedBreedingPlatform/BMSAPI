package org.ibp.api.rest.labelprinting.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;
import java.util.Map;

@AutoProperty
public class LabelsData {

	private String defaultBarcodeKey;

	private List<Map<String, String>> data;

	public LabelsData(final String defaultBarcodeKey, final List<Map<String, String>> data) {
		this.defaultBarcodeKey = defaultBarcodeKey;
		this.data = data;
	}

	public String getDefaultBarcodeKey() {
		return this.defaultBarcodeKey;
	}

	public void setDefaultBarcodeKey(final String defaultBarcodeKey) {
		this.defaultBarcodeKey = defaultBarcodeKey;
	}

	public List<Map<String, String>> getData() {
		return this.data;
	}

	public void setData(final List<Map<String, String>> data) {
		this.data = data;
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
