package org.ibp.api.rest.labelprinting.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;
import java.util.Map;

@AutoProperty
public class LabelsData {

	private Integer defaultBarcodeKey;

	private List<Map<Integer, String>> data;

	public LabelsData(final Integer defaultBarcodeKey, final List<Map<Integer, String>> data) {
		this.defaultBarcodeKey = defaultBarcodeKey;
		this.data = data;
	}

	public Integer getDefaultBarcodeKey() {
		return defaultBarcodeKey;
	}

	public void setDefaultBarcodeKey(final Integer defaultBarcodeKey) {
		this.defaultBarcodeKey = defaultBarcodeKey;
	}

	public List<Map<Integer, String>> getData() {
		return data;
	}

	public void setData(final List<Map<Integer, String>> data) {
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
