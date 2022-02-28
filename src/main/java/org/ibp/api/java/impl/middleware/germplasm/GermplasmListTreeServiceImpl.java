package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.commons.workbook.generator.RowColumnType;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserProgramStateDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListMetadata;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmListTreeService;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.rest.common.UserTreeState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Transactional
public class GermplasmListTreeServiceImpl implements GermplasmListTreeService {

	public static final String PROGRAM_LISTS = "LISTS";
	public static final String CROP_LISTS = "CROPLISTS";

	private static final String LEAD_CLASS = "lead";

	public static final String LIST_FOLDER_ID_INVALID = "list.folder.id.invalid";
	public static final String ADMIN = "ADMIN";

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	public WorkbenchDataManager workbenchDataManager;

	@Autowired
	public GermplasmDataManager germplasmDataManager;

	@Autowired
	public UserProgramStateDataManager userProgramStateDataManager;

	@Autowired
	public org.generationcp.middleware.api.germplasmlist.GermplasmListService germplasmListService;

	@Autowired
	public ProgramValidator programValidator;

	@Autowired
	public SecurityService securityService;

	@Autowired
	private UserValidator userValidator;

	@Autowired
	private GermplasmListValidator germplasmListValidator;

	private BindingResult errors;

	@Override
	public List<TreeNode> getGermplasmListChildrenNodes(final String crop, final String programUUID, final String parentId,
		final Boolean folderOnly) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.validateProgram(crop, programUUID);
		return this.getChildrenNodes(programUUID, parentId, folderOnly);
	}

	private List<TreeNode> getChildrenNodes(final String programUUID, final String parentId, final Boolean folderOnly) {

		this.germplasmListValidator.validateFolderId(parentId, programUUID, GermplasmListValidator.ListNodeType.FOLDER);

		checkNotNull(folderOnly, "list.folder.only");

		final List<TreeNode> treeNodes = new ArrayList<>();
		if (parentId == null) {
			final TreeNode cropFolderNode =
				new TreeNode(GermplasmListTreeServiceImpl.CROP_LISTS, AppConstants.CROP_LISTS.getString(), true, LEAD_CLASS,
					AppConstants.FOLDER_ICON_PNG.getString(), null);
			cropFolderNode.setNumOfChildren(this.germplasmListManager.getAllTopLevelLists(null).size());
			treeNodes.add(cropFolderNode);
			if (programUUID != null) {
				final TreeNode programFolderNode = this.getProgramFolderTreeNode(programUUID);
				programFolderNode.setNumOfChildren(this.germplasmListManager.getAllTopLevelLists(programUUID).size());
				treeNodes.add(programFolderNode);
			}
		} else {
			final List<GermplasmList> rootLists;
			if (GermplasmListTreeServiceImpl.PROGRAM_LISTS.equals(parentId)) {
				rootLists = this.germplasmListManager.getAllTopLevelLists(programUUID);
			} else if (GermplasmListTreeServiceImpl.CROP_LISTS.equals(parentId)) {
				rootLists = this.germplasmListManager.getAllTopLevelLists(null);
			} else {
				rootLists = this.germplasmListManager.getGermplasmListByParentFolderId(Integer.parseInt(parentId));
			}

			this.germplasmListManager.populateGermplasmListCreatedByName(rootLists);

			final List<UserDefinedField> listTypes = this.germplasmDataManager
				.getUserDefinedFieldByFieldTableNameAndType(RowColumnType.LIST_TYPE.getFtable(), RowColumnType.LIST_TYPE.getFtype());

			final List<TreeNode> children = TreeViewUtil.convertGermplasmListToTreeView(rootLists, folderOnly, listTypes);

			final Map<Integer, ListMetadata> allListMetaData = this.germplasmListManager.getGermplasmListMetadata(rootLists);

			for (final TreeNode newNode : children) {
				final ListMetadata nodeMetaData = allListMetaData.get(Integer.parseInt(newNode.getKey()));
				if (nodeMetaData != null) {
					if (nodeMetaData.getNumberOfChildren() > 0) {
						newNode.setIsLazy(true);
						newNode.setNumOfChildren(nodeMetaData.getNumberOfChildren());
					}
					if (!newNode.getIsFolder()) {
						newNode.setNoOfEntries(nodeMetaData.getNumberOfEntries());
					}
				}
				newNode.setParentId(parentId);
			}
			return children;
		}
		return treeNodes;
	}

	private TreeNode getProgramFolderTreeNode(final String programUUID) {
		return new TreeNode(GermplasmListTreeServiceImpl.PROGRAM_LISTS, AppConstants.PROGRAM_LISTS.getString(), true, LEAD_CLASS,
			AppConstants.FOLDER_ICON_PNG.getString(), programUUID);
	}

	@Override
	public List<TreeNode> getUserTreeState(final String crop, final String programUUID, final String userId) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.validateProgram(crop, programUUID);
		this.userValidator.validateUserId(this.errors, userId);
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		// Initialize crop and program folder nodes
		final List<TreeNode> treeNodesList = this.getChildrenNodes(programUUID, null, false);
		// Retrieve the list of "Crop List" expanded nodes
		final List<String> cropExpandedFolders = this.userProgramStateDataManager
			.getUserProgramTreeState(Integer.parseInt(userId), null, ListTreeState.GERMPLASM_LIST.name());
		this.setTreeExpandedFolders(programUUID, treeNodesList.get(0), cropExpandedFolders, false);

		// Retrieve the list of "Program List" expanded nodes
		if (StringUtils.isNotEmpty(programUUID)) {
			final List<String> programExpandedFolders = this.userProgramStateDataManager
				.getUserProgramTreeState(Integer.parseInt(userId), programUUID, ListTreeState.GERMPLASM_LIST.name());
			this.setTreeExpandedFolders(programUUID, treeNodesList.get(1), programExpandedFolders, true);
		}

		return treeNodesList;
	}

	private void setTreeExpandedFolders(final String programUUID, final TreeNode rootNode, final List<String> expandedFolders,
		final boolean alwaysExpandRootNode) {
		// If alwaysExpandRootNode = false, only retrieve children if there are other expanded sub-folders
		if (!isEmpty(expandedFolders) && (alwaysExpandRootNode || expandedFolders.size() > 1)) {
			final Map<String, TreeNode> folderParentNodeMap = new HashMap<>();
			rootNode.setChildren(this.getChildrenNodes(programUUID, rootNode.getKey(), false));
			rootNode.setExpand(true);
			rootNode.getChildren().forEach(c -> folderParentNodeMap.put(c.getKey(), rootNode));
			for (int i = 1; i < expandedFolders.size(); i++) {
				final String finalKey = StringUtils.stripStart(expandedFolders.get(i), " ");
				final Optional<Map.Entry<String, TreeNode>> parentNodeOpt =
					folderParentNodeMap.entrySet().stream().filter(entry -> entry.getKey().equals(finalKey)).findFirst();
				// Find parent node then look for the node to expand among the parent's children then finally expand that node
				if (parentNodeOpt.isPresent()) {
					final TreeNode parentNode = parentNodeOpt.get().getValue();
					final Optional<TreeNode> nodeToExpand =
						parentNode.getChildren().stream().filter(child -> child.getKey().equals(finalKey)).findFirst();
					nodeToExpand.ifPresent(node -> {
							node.setExpand(true);
							node.setChildren(this.getChildrenNodes(programUUID, finalKey, false));
							node.getChildren().forEach(c -> folderParentNodeMap.put(c.getKey(), node));
						}
					);
				}
			}

		}
	}

	@Override
	public void saveGermplasmListTreeState(final String crop, final String programUUID, final UserTreeState userTreeState) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		checkNotNull(userTreeState, "param.null", new String[] {"treeState"});
		this.validateProgram(crop, programUUID);

		final String userIdString = userTreeState.getUserId();
		this.userValidator.validateUserId(this.errors, userIdString);

		final List<String> programFolders = userTreeState.getProgramFolders();
		this.validateFolders(programFolders, programUUID);
		if(userTreeState.getCropFolders() != null) {
			final List<String> cropFolders = userTreeState.getCropFolders();
			this.validateFolders(cropFolders, programUUID);
		}
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		// Persist the Program and Crop tree state for user
		final int userId = Integer.parseInt(userIdString);
		this.userProgramStateDataManager.saveOrUpdateUserProgramTreeState(userId, programUUID,
			ListTreeState.GERMPLASM_LIST.name(),
			programFolders);
		if(userTreeState.getCropFolders() != null) {
			final List<String> cropFolders = userTreeState.getCropFolders();
			this.userProgramStateDataManager.saveOrUpdateUserProgramTreeState(userId, null,
				ListTreeState.GERMPLASM_LIST.name(),
				cropFolders);
		}
	}

	private void validateFolders(final List<String> folders, final String programUUID) {
		if (isEmpty(folders)) {
			this.errors.reject("list.folders.empty", "");
		}
		folders.forEach(nodeId -> this.germplasmListValidator
			.validateFolderId(nodeId.toUpperCase(), programUUID, GermplasmListValidator.ListNodeType.PARENT));
	}

	@Override
	public Integer createGermplasmListFolder(final String cropName, final String programUUID, final String folderName,
		final String parentId) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		this.germplasmListValidator.validateFolderName(folderName);
		this.validateProgram(cropName, programUUID);
		final Optional<GermplasmList> parentFolder =
			this.germplasmListValidator.validateFolderId(parentId, programUUID, GermplasmListValidator.ListNodeType.PARENT);

		//Validate if there is a folder with same name in parent folder
		final Integer parent = this.getFolderIdAsInteger(parentId);
		this.germplasmListValidator.validateNotSameFolderNameInParent(folderName, parent, programUUID);

		final WorkbenchUser createdBy = this.securityService.getCurrentlyLoggedInUser();
		final String dependantProgramUUID = GermplasmListHelper.calculateProgramUUID(programUUID, parentFolder, parentId);
		return this.germplasmListService.createGermplasmListFolder(createdBy.getUserid(), folderName, parent, dependantProgramUUID);
	}

	@Override
	public Integer updateGermplasmListFolderName(final String cropName, final String programUUID, final String newFolderName,
		final String folderId) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		this.validateFolderNotCropNorProgramList(folderId);
		this.germplasmListValidator.validateFolderName(newFolderName);
		this.validateProgram(cropName, programUUID);

		final GermplasmList folder =
			this.germplasmListValidator.validateFolderId(folderId, programUUID, GermplasmListValidator.ListNodeType.FOLDER).get();

		//Preventing edition using the same list name
		if (newFolderName.equalsIgnoreCase(folder.getName())) {
			return folder.getId();
		}

		//Validate if there is a folder with same name in parent folder
		this.germplasmListValidator.validateNotSameFolderNameInParent(newFolderName, folder.getParentId(), folder.getProgramUUID());

		return this.germplasmListService.updateGermplasmListFolder(newFolderName, Integer.valueOf(folderId));
	}

	@Override
	public Integer moveGermplasmListNode(final String cropName, final String programUUID, final String folderId,
		final String newParentFolderId) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		if (StringUtils.isEmpty(folderId)) {
			this.errors.reject(LIST_FOLDER_ID_INVALID, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (StringUtils.isEmpty(newParentFolderId)) {
			this.errors.reject("list.parent.id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (folderId.equals(newParentFolderId)) {
			this.errors.reject("list.move.id.same.values", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateProgram(cropName, programUUID);

		final Optional<GermplasmList> newParentFolder =
			this.germplasmListValidator.validateFolderId(newParentFolderId, programUUID, GermplasmListValidator.ListNodeType.PARENT);
		this.germplasmListValidator.validateNodeId(folderId, GermplasmListValidator.ListNodeType.FOLDER);
		this.validateFolderNotCropNorProgramList(folderId);

		final GermplasmList germplasmListToMove = this.germplasmListService.getGermplasmListById(Integer.parseInt(folderId))
			.orElseThrow(() -> {
				this.errors.reject("list.folder.id.not.exist", "");
				return new ApiRequestValidationException(this.errors.getAllErrors());
			});

		if (germplasmListToMove.getProgramUUID() != null && programUUID != null && !germplasmListToMove.getProgramUUID()
			.equals(programUUID)) {
			this.errors.reject("list.folder.id.not.exist", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.germplasmListValidator.validateFolderHasNoChildren(Integer.parseInt(folderId), "list.move.folder.has.child");

		final Integer parent = this.getFolderIdAsInteger(newParentFolderId);

		final String dependantProgramUUID = GermplasmListHelper.calculateProgramUUID(programUUID, newParentFolder, newParentFolderId);

		//Validate if there is a folder with same name in parent folder
		this.germplasmListService.getGermplasmListByParentAndName(germplasmListToMove.getName(), parent, dependantProgramUUID)
			.ifPresent(germplasmList -> {
				this.errors.reject("list.folder.name.exists", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			});

		return this.germplasmListService.moveGermplasmListFolder(Integer.parseInt(folderId), parent, dependantProgramUUID);
	}

	@Override
	public void deleteGermplasmListFolder(final String cropName, final String programUUID, final String folderId) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		if (StringUtils.isEmpty(folderId)) {
			this.errors.reject(LIST_FOLDER_ID_INVALID, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateFolderNotCropNorProgramList(folderId);
		this.validateProgram(cropName, programUUID);
		final GermplasmList folder =
			this.germplasmListValidator.validateFolderId(folderId, programUUID, GermplasmListValidator.ListNodeType.FOLDER).get();

		this.germplasmListValidator.validateFolderHasNoChildren(Integer.parseInt(folderId),"list.delete.folder.has.child");

		final WorkbenchUser createdBy = this.securityService.getCurrentlyLoggedInUser();
		if (!folder.getUserId().equals(createdBy.getUserid())) {
			this.errors.reject("list.delete.not.owner", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.germplasmListService.deleteGermplasmListFolder(Integer.parseInt(folderId));
	}

	private void validateProgram(final String cropName, final String programUUID) {
		if (!StringUtils.isEmpty(programUUID)) {
			this.programValidator.validate(new ProgramDTO(cropName, programUUID), this.errors);
			if (this.errors.hasErrors()) {
				throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
			}
		}
	}

	private void validateFolderNotCropNorProgramList(final String folderId) {
		if (folderId.equals(CROP_LISTS) || folderId.equals(PROGRAM_LISTS)) {
			this.errors.reject(LIST_FOLDER_ID_INVALID, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private Integer getFolderIdAsInteger(final String folderId) {
		return (CROP_LISTS.equals(folderId) || PROGRAM_LISTS.equals(folderId)) ? null : Integer.valueOf(folderId);
	}

	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setWorkbenchDataManager(final WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

}
