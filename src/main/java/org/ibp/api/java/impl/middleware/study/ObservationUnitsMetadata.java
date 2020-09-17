package org.ibp.api.java.impl.middleware.study;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class ObservationUnitsMetadata {

	private Long countObservationUnits;

	private Long countInstances;

	public Long getCountObservationUnits() {
		return countObservationUnits;
	}

	public void setCountObservationUnits(final Long countObservationUnits) {
		this.countObservationUnits = countObservationUnits;
	}

	public Long getCountInstances() {
		return countInstances;
	}

	public void setCountInstances(final Long countInstances) {
		this.countInstances = countInstances;
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
