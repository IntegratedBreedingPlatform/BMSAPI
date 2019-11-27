package org.ibp.api.java.germplasm;

import org.generationcp.commons.pojo.treeview.TreeNode;

import java.util.List;

public interface GermplamListService {


	List<TreeNode> getGermplasmListChildrenNodes(final String crop, final String programUUID, final String parentId, final Boolean folderOnly);

}