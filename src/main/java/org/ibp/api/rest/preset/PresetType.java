package org.ibp.api.rest.preset;

public enum PresetType {

	LABEL_PRINTING_PRESET ("LabelPrintingPreset");

	private String name;

	PresetType(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public static PresetType getEnum(final String name) {
		for (PresetType e : PresetType.values()) {
			if (name.equals(e.getName()))
				return e;
		}
		return null;
	}
}
