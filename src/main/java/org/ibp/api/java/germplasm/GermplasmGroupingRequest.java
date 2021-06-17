package org.ibp.api.java.germplasm;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class GermplasmGroupingRequest {

	private List<Integer> gids;
	private boolean preserveExistingGroup;
	private boolean includeDescendants;

	public List<Integer> getGids() {
		return gids;
	}

	public void setGids(final List<Integer> gids) {
		this.gids = gids;
	}

	public boolean isPreserveExistingGroup() {
		return preserveExistingGroup;
	}

	public void setPreserveExistingGroup(final boolean preserveExistingGroup) {
		this.preserveExistingGroup = preserveExistingGroup;
	}

	public boolean isIncludeDescendants() {
		return includeDescendants;
	}

	public void setIncludeDescendants(final boolean includeDescendants) {
		this.includeDescendants = includeDescendants;
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
