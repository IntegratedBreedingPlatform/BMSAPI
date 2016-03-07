
package org.ibp.api.domain.germplasm;

import java.util.ArrayList;
import java.util.List;

public class DescendantTreeTreeNode {

	private Integer germplasmId;
	private String name;

	private Integer progenitors;
	private Integer parent1Id;
	private Integer parent2Id;
	private Integer methodId;
	private Integer managementGroupId;

	private List<DescendantTreeTreeNode> children = new ArrayList<>();

	public Integer getGermplasmId() {
		return this.germplasmId;
	}

	public void setGermplasmId(final Integer germplasmId) {
		this.germplasmId = germplasmId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<DescendantTreeTreeNode> getChildren() {
		return this.children;
	}

	public void setChildren(final List<DescendantTreeTreeNode> children) {
		this.children = children;
	}

	public Integer getProgenitors() {
		return this.progenitors;
	}

	public void setProgenitors(final Integer progenitors) {
		this.progenitors = progenitors;
	}

	public Integer getParent1Id() {
		return this.parent1Id;
	}

	public void setParent1Id(final Integer parent1Id) {
		this.parent1Id = parent1Id;
	}

	public Integer getParent2Id() {
		return this.parent2Id;
	}

	public void setParent2Id(final Integer parent2Id) {
		this.parent2Id = parent2Id;
	}

	public Integer getMethodId() {
		return this.methodId;
	}

	public void setMethodId(final Integer methodId) {
		this.methodId = methodId;
	}

	public Integer getManagementGroupId() {
		return this.managementGroupId;
	}

	public void setManagementGroupId(Integer managementGroupId) {
		this.managementGroupId = managementGroupId;
	}
}
