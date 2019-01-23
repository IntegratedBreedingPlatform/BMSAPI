package org.ibp.api.rest.labelprinting;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class Field {

	private String id;

	private String name;

	public Field(final String id, final String name) {
		this.id = id;
		this.name = name;
	}

	public Field (final MeasurementVariable measurementVariable) {
		this.id = String.valueOf(measurementVariable.getTermId());
		this.name = String.valueOf(measurementVariable.getAlias());
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
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
