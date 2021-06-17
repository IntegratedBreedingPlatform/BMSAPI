package org.ibp.api.domain.germplasm;

import org.pojomatic.Pojomatic;

import java.util.List;

public class GermplasmUngroupingResponse {

	private List<Integer> unfixedGids;
	private Integer numberOfGermplasmWithoutGroup;

	public List<Integer> getUnfixedGids() {
		return unfixedGids;
	}

	public void setUnfixedGids(final List<Integer> unfixedGids) {
		this.unfixedGids = unfixedGids;
	}

	public Integer getNumberOfGermplasmWithoutGroup() {
		return numberOfGermplasmWithoutGroup;
	}

	public void setNumberOfGermplasmWithoutGroup(final Integer numberOfGermplasmWithoutGroup) {
		this.numberOfGermplasmWithoutGroup = numberOfGermplasmWithoutGroup;
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
