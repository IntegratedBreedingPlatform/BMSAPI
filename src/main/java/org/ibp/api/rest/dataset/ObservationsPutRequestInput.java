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
	private boolean draftMode = false;
	private List<List<String>> data;

	public boolean isProcessWarnings() {
		return this.processWarnings;
	}

	public void setProcessWarnings(final boolean processWarnings) {
		this.processWarnings = processWarnings;
	}

	public boolean isDraftMode() {
		return this.draftMode;
	}

	public void setDraftMode(final boolean draftMode) {
		this.draftMode = draftMode;
	}

	public List<List<String>> getData() {
		return this.data;
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
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

}
