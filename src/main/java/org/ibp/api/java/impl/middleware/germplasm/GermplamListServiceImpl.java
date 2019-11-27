package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListMetadata;
import org.generationcp.middleware.pojos.workbench.Project;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplamListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GermplamListServiceImpl implements GermplamListService {

	private static final String PROGRAM_LISTS = "LISTS";
	private static final String CROP_LISTS = "CROPLISTS";
	private static final String LEAD_CLASS = "lead";
	private static final int BATCH_SIZE = 500;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	public WorkbenchDataManager workbenchDataManager;

	private BindingResult errors;

	@Override
	public List<TreeNode> getGermplasmListChildrenNodes(final String crop, final String programUUID, final String parentId,
		final Boolean folderOnly) {

		errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());

		this.validateProgramUUID(crop, programUUID);
		this.validateParentId(parentId, programUUID);
		this.validateFolderOnly(folderOnly);

		final List<TreeNode> treeNodes = new ArrayList<>();
		if (parentId == null) {
			treeNodes.add(new TreeNode(GermplamListServiceImpl.CROP_LISTS, AppConstants.CROP_LISTS.getString(), true, LEAD_CLASS,
				AppConstants.FOLDER_ICON_PNG.getString(), null));
			if (programUUID != null) {
				treeNodes.add(new TreeNode(GermplamListServiceImpl.PROGRAM_LISTS, AppConstants.PROGRAM_LISTS.getString(), true, LEAD_CLASS,
						AppConstants.FOLDER_ICON_PNG.getString(), programUUID));
			}
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

	private void validateProgramUUID(final String crop, final String programUUID) {
		if (!StringUtils.isEmpty(programUUID)) {
			final Project project = workbenchDataManager.getProjectByUuidAndCrop(programUUID, crop);
			if (project == null) {
				this.errors.reject("germplasm.list.project.invalid", "");
				throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
			}
		}
	}

	private void validateParentId(final String parentId, final String programUUID) {
		if (parentId != null && !parentId.equals(PROGRAM_LISTS) && !parentId.equals(CROP_LISTS) && !Util.isPositiveInteger(parentId)) {
			this.errors.reject("germplasm.list.parent.id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if ((PROGRAM_LISTS.equals(parentId) || Util.isPositiveInteger(parentId)) && programUUID == null) {
			this.errors.reject("germplasm.list.project.mandatory", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		//TODO Missing validation, when parentId is integer and programUUID is not null, then the parentId type must be FOLDER

	}

	private void validateFolderOnly(final Boolean folderOnly) {
		if (folderOnly == null) {
			this.errors.reject("germplasm.list.folder.only", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
