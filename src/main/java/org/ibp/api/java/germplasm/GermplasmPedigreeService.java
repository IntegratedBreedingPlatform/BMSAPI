package org.ibp.api.java.germplasm;

import org.generationcp.middleware.api.germplasm.pedigree.GermplasmTreeNode;

public interface GermplasmPedigreeService {

	GermplasmTreeNode getGermplasmPedigreeTree(Integer gid, Integer level, boolean includeDerivativeLine);
}
