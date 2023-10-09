package org.ibp.api.java.crossplan;

import org.generationcp.commons.pojo.treeview.TreeNode;

import java.util.List;

public interface CrossPlanTreeService {

    Integer createCrossPlanTreeFolder(String cropName, String programUUID, Integer parentId, String folderName);

    Integer updateCrossPlanTreeFolder(String cropName, String programUUID, Integer parentId, String newFolderName);

    void deleteCrossPlanFolder(String cropName, String programUUID, Integer folderId);

    TreeNode moveCrossPlanNode(String cropName, String programUUID, Integer nodeId, Integer newParentFolderId);

    List<TreeNode> geCrossPlanTree(String parentKey, String programUUID);
}
