package org.ibp.api.java.study;

import org.generationcp.commons.pojo.treeview.TreeNode;

import java.util.List;

public interface StudyTreeService {

	Integer createStudyTreeFolder(String cropName, String programUUID, Integer parentId, String folderName);

	Integer updateStudyTreeFolder(String cropName, String programUUID, Integer parentId, String newFolderName);

	void deleteStudyFolder(String cropName, String programUUID, Integer folderId);

	TreeNode moveStudyFolder(String cropName, String programUUID, Integer folderId, Integer newParentFolderId);

	List<TreeNode> getStudyTree(String parentKey, String programUUID);

}
