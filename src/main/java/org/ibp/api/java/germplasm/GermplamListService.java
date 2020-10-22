package org.ibp.api.java.germplasm;

import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.pojos.GermplasmList;

import java.util.List;

public interface GermplamListService {

	List<TreeNode> getGermplasmListChildrenNodes(final String crop, final String programUUID, final String parentId, final Boolean folderOnly);

	GermplasmList getGermplasmList(Integer germplasmListId);

	List<GermplasmListTypeDTO> getGermplasmListTypes();

}