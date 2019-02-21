package org.ibp.api.rest.labelprinting.domain;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class Field {

	private Integer id;

	private String name;

	public Field(final Integer id, final String name) {
		this.id = id;
		this.name = name;
	}

	public Field (final MeasurementVariable measurementVariable) {
		this.id = measurementVariable.getTermId();
		this.name = String.valueOf(measurementVariable.getAlias());
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
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
