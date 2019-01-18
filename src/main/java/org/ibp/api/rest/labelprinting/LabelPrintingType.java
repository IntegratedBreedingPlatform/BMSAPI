package org.ibp.api.rest.labelprinting;

public enum LabelPrintingType {

	DATASET(1);

	private Integer code;

	LabelPrintingType(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(final Integer value) {
		this.code = code;
	}

	public static LabelPrintingType getEnumByCode(final Integer code) {
		for (LabelPrintingType e : LabelPrintingType.values()) {
			if (code == e.getCode())
				return e;
		}
		return null;
	}
}
