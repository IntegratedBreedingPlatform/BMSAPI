package org.ibp.api.java.germplasm;

import org.generationcp.commons.pojo.treeview.TreeNode;
import org.ibp.api.rest.common.UserTreeState;

import java.util.List;

public interface GermplasmListTreeService {

	List<TreeNode> getGermplasmListChildrenNodes(
		String crop, String programUUID, String parentId, Boolean folderOnly);

	List<TreeNode> getUserTreeState(String crop, String programUUID, String userId);

	void saveGermplasmListTreeState(String crop, String programUUID, UserTreeState userTreeState);

	Integer createGermplasmListFolder(String cropName, String programUUID, String folderName, String parentId);

	Integer updateGermplasmListFolderName(String cropName, String programUUID, String newFolderName, String folderId);

	TreeNode moveGermplasmListNode(String cropName, String programUUID, String folderId, String newParentFolderId);

	void deleteGermplasmListFolder(String cropName, String programUUID, String folderId);

}
