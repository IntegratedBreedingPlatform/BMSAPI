package org.ibp.api.java.impl.middleware.study;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class ObservationUnitsMetadata {

	private Long selectedObservationUnits;

	private Long selectedInstances;

	public Long getSelectedObservationUnits() {
		return selectedObservationUnits;
	}

	public void setSelectedObservationUnits(final Long selectedObservationUnits) {
		this.selectedObservationUnits = selectedObservationUnits;
	}

	public Long getSelectedInstances() {
		return selectedInstances;
	}

	public void setSelectedInstances(final Long selectedInstances) {
		this.selectedInstances = selectedInstances;
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
