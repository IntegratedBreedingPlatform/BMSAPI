package org.ibp.api.rest.labelprinting;

public enum LabelPrintingType {

	DATASET (1);

	private Integer value;

	LabelPrintingType (final Integer value) {
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(final Integer value) {
		this.value = value;
	}
}
