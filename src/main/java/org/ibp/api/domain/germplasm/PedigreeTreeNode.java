
package org.ibp.api.domain.germplasm;

import java.util.ArrayList;
import java.util.List;

public class PedigreeTreeNode {

	private String germplasmId;
	private String name;
	private List<PedigreeTreeNode> parents = new ArrayList<>();

	public String getGermplasmId() {
		return this.germplasmId;
	}

	public void setGermplasmId(String germplasmId) {
		this.germplasmId = germplasmId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<PedigreeTreeNode> getParents() {
		return this.parents;
	}

	public void setParents(List<PedigreeTreeNode> parents) {
		this.parents = parents;
	}

}
