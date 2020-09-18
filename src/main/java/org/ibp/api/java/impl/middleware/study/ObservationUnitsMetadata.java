package org.ibp.api.java.impl.middleware.study;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class ObservationUnitsMetadata {

	private Long observationUnitsCount;

	private Long instancesCount;

	public Long getObservationUnitsCount() {
		return observationUnitsCount;
	}

	public void setObservationUnitsCount(final Long observationUnitsCount) {
		this.observationUnitsCount = observationUnitsCount;
	}

	public Long getInstancesCount() {
		return instancesCount;
	}

	public void setInstancesCount(final Long instancesCount) {
		this.instancesCount = instancesCount;
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
