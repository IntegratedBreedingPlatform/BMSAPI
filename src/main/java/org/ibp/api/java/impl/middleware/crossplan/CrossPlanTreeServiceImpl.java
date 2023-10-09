package org.ibp.api.java.impl.middleware.crossplan;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.ibp.api.java.crossplan.CrossPlanTreeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
public class CrossPlanTreeServiceImpl implements CrossPlanTreeService {

    @Resource
    private org.generationcp.middleware.service.api.crossplan.CrossPlanTreeService crossPlanTreeService;

    @Override
    public Integer createCrossPlanTreeFolder(String cropName, String programUUID, Integer parentId, String folderName) {
        // TODO Validator
        return this.crossPlanTreeService.createCrossPlanTreeFolder(parentId, folderName, programUUID);
    }

    @Override
    public Integer updateCrossPlanTreeFolder(String cropName, String programUUID, Integer parentId, String newFolderName) {
        // TODO Validator
        return this.crossPlanTreeService.updateCrossPlanTreeFolder(parentId,newFolderName);
    }

    @Override
    public void deleteCrossPlanFolder(String cropName, String programUUID, Integer folderId) {
        // TODO Validator
        this.crossPlanTreeService.deleteCrossPlanFolder(folderId);
    }

    @Override
    public TreeNode moveCrossPlanNode(String cropName, String programUUID, Integer nodeId, Integer newParentFolderId) {

        this.crossPlanTreeService.moveCrossPlanNode(nodeId, newParentFolderId);
        final List<Reference> folders = this.crossPlanTreeService.getChildrenOfFolder(newParentFolderId, programUUID);
        return TreeViewUtil.convertStudyFolderReferencesToTreeView(folders, true).get(0);
    }

    @Override
    public List<TreeNode> geCrossPlanTree(String parentKey, String programUUID) {
        List<TreeNode> nodes = new ArrayList<>();
        if (StringUtils.isBlank(parentKey)) {
            final TreeNode rootNode = new TreeNode(DmsProject.SYSTEM_FOLDER_ID.toString(), AppConstants.CROSS_PLAN.getString(), true, null);
            nodes.add(rootNode);
        } else if (NumberUtils.isNumber(parentKey)) {
            final List<Reference> folders = this.crossPlanTreeService.getChildrenOfFolder(Integer.valueOf(parentKey), programUUID);
            nodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(folders, true);
        }
        return nodes;
    }
}
