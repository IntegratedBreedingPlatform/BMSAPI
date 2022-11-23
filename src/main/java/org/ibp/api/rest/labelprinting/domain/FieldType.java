package org.ibp.api.rest.labelprinting.domain;

public enum FieldType {

	VARIABLE("VARIABLE"),
	STATIC("STATIC"),
	NAME("NAME");

	private final String name;

	FieldType(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
