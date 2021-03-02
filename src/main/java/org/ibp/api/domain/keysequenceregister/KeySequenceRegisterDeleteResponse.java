package org.ibp.api.domain.keysequenceregister;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class KeySequenceRegisterDeleteResponse {

	private List<String> deletedPrefixes;
	private List<String> undeletedPrefixes;

	public KeySequenceRegisterDeleteResponse(final List<String> deletedPrefixes, final List<String> undeletedPrefixes) {
		this.deletedPrefixes = deletedPrefixes;
		this.undeletedPrefixes = undeletedPrefixes;
	}

	public List<String> getDeletedPrefixes() {
		return this.deletedPrefixes;
	}

	public void setDeletedPrefixes(final List<String> deletedPrefixes) {
		this.deletedPrefixes = deletedPrefixes;
	}

	public List<String> getUndeletedPrefixes() {
		return this.undeletedPrefixes;
	}

	public void setUndeletedPrefixes(final List<String> undeletedPrefixes) {
		this.undeletedPrefixes = undeletedPrefixes;
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

}
