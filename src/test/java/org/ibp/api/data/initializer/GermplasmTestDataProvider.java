package org.ibp.api.data.initializer;

import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.generationcp.middleware.pojos.Name;

import com.google.common.collect.Lists;

public class GermplasmTestDataProvider {

	/**
	 * Method to create single germplasm
	 * @return germplasm
	 */
	public static Germplasm createGermplasm() {
		final Germplasm germplasm = new Germplasm();
		germplasm.setGid(3);
		germplasm.setGpid1(1);
		germplasm.setGpid2(2);
		germplasm.setMethodId(1);
		germplasm.setLocationId(1);

		return germplasm;
	}

	public static GermplasmPedigreeTreeNode createNode(Integer gid, String name, Integer parent1Gid, String parent1Name, Integer parent2Gid,
			String parent2Name) {
		GermplasmPedigreeTreeNode mainNode = new GermplasmPedigreeTreeNode();
		Germplasm germplasm = setGidAndNameToGermplasm(gid, name);
		mainNode.setGermplasm(germplasm);

		GermplasmPedigreeTreeNode parent1Node = new GermplasmPedigreeTreeNode();
		Germplasm germplasmParent1 = setGidAndNameToGermplasm(parent1Gid, parent1Name);
		parent1Node.setGermplasm(germplasmParent1);

		GermplasmPedigreeTreeNode parent2Node = new GermplasmPedigreeTreeNode();
		Germplasm germplasmParent2 = setGidAndNameToGermplasm(parent2Gid, parent2Name);
		parent2Node.setGermplasm(germplasmParent2);

		mainNode.setLinkedNodes(Lists.newArrayList(parent1Node, parent2Node));

		return mainNode;
	}

	private static Germplasm setGidAndNameToGermplasm(Integer gid, String name) {
		Germplasm germplasm = new Germplasm();
		germplasm.setGid(gid);
		Name preferredName = new Name();
		preferredName.setNval(name);
		germplasm.setPreferredName(preferredName);

		return germplasm;
	}

}
