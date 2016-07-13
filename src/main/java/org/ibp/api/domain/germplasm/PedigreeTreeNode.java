
package org.ibp.api.domain.germplasm;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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

	/**
	 * Conceptually in a pedigree tree, the nodes are parents, not children. So the POJO field is named "parents". However, the D3 library
	 * code we use to visualise the tree (see ancestorTree.js) relies on the property name being "children" so this is a simple fix to
	 * satisfy D3 tree's node naming requirement for JSON field. Changing the D3 library code to recognise "parents" field was too much
	 * change. This is simpler alternative.
	 * 
	 */
	@JsonProperty("children")
	public List<PedigreeTreeNode> getParents() {
		return this.parents;
	}

	public void setParents(List<PedigreeTreeNode> parents) {
		this.parents = parents;
	}

}
