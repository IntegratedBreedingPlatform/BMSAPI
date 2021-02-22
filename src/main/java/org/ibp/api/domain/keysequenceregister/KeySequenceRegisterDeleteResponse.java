package org.ibp.api.domain.keysequenceregister;

import java.util.List;

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

}
