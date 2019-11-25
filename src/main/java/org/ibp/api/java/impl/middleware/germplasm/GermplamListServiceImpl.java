package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListMetadata;
import org.ibp.api.java.germplasm.GermplamListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.NEVER)
public class GermplamListServiceImpl implements GermplamListService {

	protected static final String PROGRAM_LISTS = "LISTS";
	protected static final String CROP_LISTS = "CROPLISTS";
	public static final int BATCH_SIZE = 500;


	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	public WorkbenchDataManager workbenchDataManager;

	@Override
	public List<TreeNode> getGermplasmListChildrenNodes(final String crop, final String programUUID, final String parentId,
		final Boolean folderOnly) {

		//Validate program

		//Validate parentId, it can be null, PROGRAM_LISTS, CROP_LISTS or Integer

		//Validate folderOnly can not be null

		final List<TreeNode> treeNodes = new ArrayList<>();
		if (parentId == null) {
			treeNodes.add(new TreeNode(GermplamListServiceImpl.CROP_LISTS, AppConstants.CROP_LISTS.getString(), true, "lead",
				AppConstants.FOLDER_ICON_PNG.getString(), null));
			treeNodes.add(new TreeNode(GermplamListServiceImpl.PROGRAM_LISTS, AppConstants.PROGRAM_LISTS.getString(), true, "lead",
				AppConstants.FOLDER_ICON_PNG.getString(), programUUID));
		} else {
			final List<GermplasmList> rootLists;
			if (GermplamListServiceImpl.PROGRAM_LISTS.equals(parentId)) {
				rootLists = this.germplasmListManager.getAllTopLevelLists(programUUID);
			} else if (GermplamListServiceImpl.CROP_LISTS.equals(parentId)) {
				rootLists = this.germplasmListManager.getAllTopLevelLists(null);
			} else {
				rootLists = this.germplasmListManager.getGermplasmListByParentFolderIdBatched(Integer.parseInt(parentId), programUUID, GermplamListServiceImpl.BATCH_SIZE);
			}

			final List<TreeNode> childNodes = TreeViewUtil.convertGermplasmListToTreeView(rootLists, folderOnly);

			final Map<Integer, ListMetadata> allListMetaData = this.germplasmListManager.getGermplasmFolderMetadata(rootLists);

			for (final TreeNode newNode : childNodes) {
				final ListMetadata nodeMetaData = allListMetaData.get(Integer.parseInt(newNode.getKey()));
				if (nodeMetaData != null && nodeMetaData.getNumberOfChildren() > 0) {
					newNode.setIsLazy(true);
				}
			}
			return childNodes;
		}
		return treeNodes;
	}
}