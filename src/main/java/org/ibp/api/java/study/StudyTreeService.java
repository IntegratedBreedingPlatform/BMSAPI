package org.ibp.api.java.study;

import org.generationcp.commons.pojo.treeview.TreeNode;

import java.util.List;

public interface StudyTreeService {

	Integer createStudyTreeFolder(String cropName, String programUUID, Integer parentId, String folderName);

	Integer updateStudyTreeFolder(final String cropName, String programUUID, int parentId, String newFolderName);

	void deleteStudyFolder(final String cropName, final String programUUID, Integer folderId);

	TreeNode moveStudyFolder(final String cropName, final String programUUID, Integer folderId, Integer newParentFolderId);

	List<TreeNode> getStudyTree(String parentKey, String programUUID);

}
