package org.ibp.api.rest.dataset;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

/**
 * Created by clarysabel on 11/23/18.
 */
@AutoProperty
public class ObservationsPutRequestInput {

	private boolean processWarnings;

	private List<List<String>> data;

	public boolean isProcessWarnings() {
		return processWarnings;
	}

	public void setProcessWarnings(final boolean processWarnings) {
		this.processWarnings = processWarnings;
	}

	public List<List<String>> getData() {
		return data;
	}

	public void setData(final List<List<String>> data) {
		this.data = data;
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
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
