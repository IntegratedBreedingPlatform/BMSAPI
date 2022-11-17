package org.ibp.api.rest.labelprinting.domain;

public enum FieldType {

	VARIABLE("VARIABLE"),
	VIRTUAL_VARIABLE("VIRTUAL_VARIABLE"),
	NAME("NAME");

	private final String name;

	FieldType(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
