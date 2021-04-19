package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.commons.workbook.generator.RowColumnType;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserProgramStateDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListMetadata;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmListService;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Service
@Transactional
public class GermplasmListServiceImpl implements GermplasmListService {

	public static final String PROGRAM_LISTS = "LISTS";
	public static final String CROP_LISTS = "CROPLISTS";
	public static final int BATCH_SIZE = 500;
	public static final int NAME_MAX_LENGTH = 50;

	private static final String LEAD_CLASS = "lead";

	// List entry params
	static final String ENTRY_CODE = "entryCode";
	static final String SEED_SOURCE = "seedSource";
	static final String GROUP_NAME = "groupName";

	private enum ListNodeType {
		PARENT("parent"),
		FOLDER("folder");

		private final String value;

		ListNodeType(final String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	public WorkbenchDataManager workbenchDataManager;

	@Autowired
	public GermplasmDataManager germplasmDataManager;

	@Autowired
	public UserProgramStateDataManager userProgramStateDataManager;

	@Autowired
	public GermplasmSearchService germplasmSearchService;

	@Autowired
	public org.generationcp.middleware.api.germplasmlist.GermplasmListService germplasmListService;

	@Autowired
	public ProgramValidator programValidator;

	@Autowired
	public SecurityService securityService;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	@Autowired
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private UserValidator userValidator;

	private BindingResult errors;

	@Override
	public List<TreeNode> getGermplasmListChildrenNodes(final String crop, final String programUUID, final String parentId,
		final Boolean folderOnly) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		this.validateProgram(crop, programUUID);
		return this.getChildrenNodes(programUUID, parentId, folderOnly);
	}

	private List<TreeNode> getChildrenNodes(final String programUUID, final String parentId, final Boolean folderOnly) {
		this.validateNodeId(parentId, programUUID, ListNodeType.PARENT);
		checkNotNull(folderOnly, "list.folder.only");

		final List<TreeNode> treeNodes = new ArrayList<>();
		if (parentId == null) {
			final TreeNode cropFolderNode = new TreeNode(GermplasmListServiceImpl.CROP_LISTS, AppConstants.CROP_LISTS.getString(), true, LEAD_CLASS,
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
			if (GermplasmListServiceImpl.PROGRAM_LISTS.equals(parentId)) {
				rootLists = this.germplasmListManager.getAllTopLevelLists(programUUID);
			} else if (GermplasmListServiceImpl.CROP_LISTS.equals(parentId)) {
				rootLists = this.germplasmListManager.getAllTopLevelLists(null);
			} else {
				rootLists = this.germplasmListManager.getGermplasmListByParentFolderIdBatched(Integer.parseInt(parentId), programUUID, GermplasmListServiceImpl.BATCH_SIZE);
			}

			this.germplasmListManager.populateGermplasmListCreatedByName(rootLists);

			final List<UserDefinedField> listTypes = this.germplasmDataManager
					.getUserDefinedFieldByFieldTableNameAndType(RowColumnType.LIST_TYPE.getFtable(), RowColumnType.LIST_TYPE.getFtype());

			final List<TreeNode> childNodes = TreeViewUtil.convertGermplasmListToTreeView(rootLists, folderOnly, listTypes);

			final Map<Integer, ListMetadata> allListMetaData = this.germplasmListManager.getGermplasmListMetadata(rootLists);

			for (final TreeNode newNode : childNodes) {
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
			return childNodes;
		}
		return treeNodes;
	}

	private TreeNode getProgramFolderTreeNode(final String programUUID) {
		return new TreeNode(GermplasmListServiceImpl.PROGRAM_LISTS, AppConstants.PROGRAM_LISTS.getString(), true, LEAD_CLASS,
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
		final List<String> treeFolders = this.userProgramStateDataManager
			.getUserProgramTreeState(Integer.parseInt(userId), programUUID, ListTreeState.GERMPLASM_LIST.name());
		if (!CollectionUtils.isEmpty(treeFolders)) {
			final TreeNode programRootNode = treeNodesList.get(1);
			programRootNode.setChildren(this.getChildrenNodes(programUUID, programRootNode.getKey(), false));
			TreeNode parentNode = programRootNode;
			for (int i=1; i<treeFolders.size(); i++) {
				final String finalKey = StringUtils.stripStart(treeFolders.get(i), " ");
				final Optional<TreeNode> parentNodeOpt = parentNode.getChildren().stream().filter(c -> c.getKey().equals(finalKey)).findFirst();
				if (parentNodeOpt.isPresent()) {
					parentNode = parentNodeOpt.get();
					parentNode.setChildren(this.getChildrenNodes(programUUID, finalKey, false));
				}
			}

		}

		return treeNodesList;
	}

	@Override
	public GermplasmList getGermplasmList(final Integer germplasmListId) {
		return this.germplasmListManager.getGermplasmListById(germplasmListId);
	}

	@Override
	public GermplasmListGeneratorDTO create(final GermplasmListGeneratorDTO request) {

		final String currentProgram = ContextHolder.getCurrentProgram();

		// validations

		checkNotNull(request, "param.null", new String[] {"request"});
		checkNotNull(request.getDate(), "param.null", new String[] {"date"});

		if (!StringUtils.isBlank(request.getDescription())) {
			checkArgument(request.getDescription().length() <= 255, "text.field.max.length", new String[] {"description", "255"});
		}

		if (!StringUtils.isBlank(request.getNotes())) {
			checkArgument(request.getNotes().length() <= 65535, "text.field.max.length", new String[] {"notes", "65535"});
		}

		final String type = request.getType();
		checkNotNull(type, "param.null", new String[] {"type"});
		if (this.getGermplasmListTypes().stream().noneMatch(typeDTO -> typeDTO.getCode().equals(type))) {
			throw new ApiValidationException("", "error.germplasmlist.save.type.not.exists", type);
		}

		final String name = request.getName();
		this.validateListName(currentProgram, name);

		final String parentFolderId = request.getParentFolderId();
		checkNotNull(parentFolderId, "param.null", new String[] {"parentFolderId"});
		this.validateNodeId(parentFolderId, currentProgram, ListNodeType.PARENT);

		// process and assign defaults + more validations
		this.processEntries(request, currentProgram);

		// properties that depend on CROP/PROGRAM folder
		int status = GermplasmList.Status.LIST.getCode();
		String programUUID = null;
		// If the germplasm list is saved in 'Crop lists' folder, the programUUID should be null
		// so that the germplasm list will be accessible to all programs of the same crop.
		if (CROP_LISTS.equals(parentFolderId)) {
			// list should be locked by default if it is saved in 'Crop lists' folder.
			status = GermplasmList.Status.LOCKED_LIST.getCode();
		} else {
			programUUID = currentProgram;
		}

		if (CROP_LISTS.equals(parentFolderId) || PROGRAM_LISTS.equals(parentFolderId)) {
			request.setParentFolderId(null);
		}

		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();

		// finally save
		return this.germplasmListService.create(request, status, programUUID, loggedInUser);
	}

	private void processEntries(final GermplasmListGeneratorDTO request, final String currentProgram) {

		// resolve/validate composite and entries

		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite = request.getSearchComposite();
		checkArgument(!CollectionUtils.isEmpty(request.getEntries()) || searchComposite != null && searchComposite.isValid(),
			"error.germplasmlist.save.entries.or.composite");

		if (CollectionUtils.isEmpty(request.getEntries())) {
			if (!CollectionUtils.isEmpty(searchComposite.getItemIds())) {
				request.setEntries(searchComposite.getItemIds().stream().map(gid -> {
					final GermplasmListGeneratorDTO.GermplasmEntryDTO entryDTO = new GermplasmListGeneratorDTO.GermplasmEntryDTO();
					entryDTO.setGid(gid);
					return entryDTO;
				}).collect(Collectors.toList()));
			} else {
				final List<GermplasmSearchResponse> list =
					this.germplasmSearchService.searchGermplasm(searchComposite.getSearchRequest(), null, currentProgram);
				checkArgument(!list.isEmpty(), "search.composite.empty.result");
				request.setEntries(list.stream().map(germplasmSearchResponse -> {
					final GermplasmListGeneratorDTO.GermplasmEntryDTO entryDTO = new GermplasmListGeneratorDTO.GermplasmEntryDTO();
					entryDTO.setGid(germplasmSearchResponse.getGid());
					return entryDTO;
				}).collect(Collectors.toList()));
			}
		}

		// process entries

		final List<Integer> gids = request.getEntries()
			.stream().map(GermplasmListGeneratorDTO.GermplasmEntryDTO::getGid).collect(Collectors.toList());
		final Map<Integer, Germplasm> germplasmMap = this.germplasmDataManager.getGermplasms(gids)
			.stream().collect(Collectors.toMap(Germplasm::getGid, Function.identity()));
		final Map<Integer, String> crossExpansions =
			this.pedigreeService.getCrossExpansionsBulk(new HashSet<>(gids), null, this.crossExpansionProperties);
		final Map<Integer, String> plotCodeValuesByGIDs = this.germplasmService.getPlotCodeValues(new HashSet<>(gids));

		int entryNo = 1;
		boolean hasEntryNo = false;
		boolean hasEntryCode = false;
		boolean hasEntryCodeEmpty = false;
		boolean hasSeedSource = false;
		boolean hasSeedSourceEmpty = false;
		boolean hasGroupName = false;
		boolean hasGroupNameEmpty = false;

		for (final GermplasmListGeneratorDTO.GermplasmEntryDTO entry : request.getEntries()) {
			final Integer gid = entry.getGid();
			if (gid == null) {
				throw new ApiValidationException("", "error.germplasmlist.save.gid");
			} else if (germplasmMap.get(gid) == null) {
				throw new ApiValidationException("", "error.germplasmlist.save.gid.not.exists", gid);
			}

			if (entry.getEntryNo() == null) {
				entry.setEntryNo(entryNo++);
			} else {
				hasEntryNo = true;
			}

			if (isBlank(entry.getEntryCode())) {
				entry.setEntryCode(String.valueOf(entry.getEntryNo()));
				hasEntryCodeEmpty = true;
			} else {
				hasEntryCode = true;
			}

			if (isBlank(entry.getSeedSource())) {
				entry.setSeedSource(plotCodeValuesByGIDs.get(gid));
				hasSeedSourceEmpty = true;
			} else {
				hasSeedSource = true;
			}

			if (isBlank(entry.getGroupName())) {
				entry.setGroupName(crossExpansions.get(gid));
				hasGroupNameEmpty = true;
			} else {
				hasGroupName = true;
			}
		}
		if (hasEntryNo && entryNo > 1) {
			throw new ApiValidationException("", "error.germplasmlist.save.entryno.gaps");
		}
		if (hasEntryCode && hasEntryCodeEmpty) {
			throw new ApiValidationException("", "error.germplasmlist.save.gaps", ENTRY_CODE);
		}
		if (hasSeedSource && hasSeedSourceEmpty) {
			throw new ApiValidationException("", "error.germplasmlist.save.gaps", SEED_SOURCE);
		}
		if (hasGroupName && hasGroupNameEmpty) {
			throw new ApiValidationException("", "error.germplasmlist.save.gaps", GROUP_NAME);
		}

	}

	@Override
	public List<GermplasmListTypeDTO> getGermplasmListTypes() {
		final List<UserDefinedField> germplasmListTypes = this.germplasmListManager.getGermplasmListTypes();
		return germplasmListTypes.stream()
			.map(userDefinedField -> {
				final GermplasmListTypeDTO germplasmListTypeDTO = new GermplasmListTypeDTO();
				germplasmListTypeDTO.setCode(userDefinedField.getFcode());
				germplasmListTypeDTO.setId(userDefinedField.getFldno());
				germplasmListTypeDTO.setName(userDefinedField.getFname());
				return germplasmListTypeDTO;
			})
			.collect(Collectors.toList());
	}

	@Override
	public void addGermplasmEntriesToList(final Integer germplasmListId,
		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite, final String programUUID) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		if (!Util.isPositiveInteger(String.valueOf(germplasmListId))) {
			this.errors.reject("list.id.invalid", new String[] {germplasmListId.toString()}, "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		this.searchCompositeDtoValidator.validateSearchCompositeDto(searchComposite, this.errors);

		final GermplasmList germplasmList = this.germplasmListService.getGermplasmListById(germplasmListId)
			.orElseThrow(() -> {
				this.errors.reject("list.id.invalid", new String[] {germplasmListId.toString()}, "");
				return new ResourceNotFoundException(this.errors.getAllErrors().get(0));
			});

		if (germplasmList.isFolder()) {
			this.errors.reject("list.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (germplasmList.isLockedList()) {
			this.errors.reject("list.locked", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!CollectionUtils.isEmpty(searchComposite.getItemIds())) {
			this.germplasmValidator.validateGids(this.errors, new ArrayList<>(searchComposite.getItemIds()));
		}

		this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, programUUID);
}

	@Override
	public Integer createGermplasmListFolder(final String cropName, final String programUUID, final String folderName,
		final String parentId) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		//TODO: remove this validation once we can create folder with CROP_LIST as parent
		if (parentId.equals(CROP_LISTS)) {
			this.errors.reject("list.parent.id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateFolderName(folderName);
		this.validateProgram(cropName, programUUID);
		this.validateNodeId(parentId, programUUID, ListNodeType.PARENT);

		//Validate if there is a folder with same name in parent folder
		final Integer parent = this.getFolderIdAsInteger(parentId);
		this.validateNotSameFolderNameInParent(folderName, parent, programUUID);

		final WorkbenchUser createdBy = this.securityService.getCurrentlyLoggedInUser();
		return this.germplasmListService.createGermplasmListFolder(createdBy.getUserid(), folderName, parent, programUUID);
	}

	@Override
	public Integer updateGermplasmListFolderName(final String cropName, final String programUUID, final String newFolderName,
		final String folderId) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		this.validateFolderNotCropNorProgramList(folderId);
		this.validateFolderName(newFolderName);
		this.validateProgram(cropName, programUUID);
		this.validateNodeId(folderId, programUUID, ListNodeType.FOLDER);

		final GermplasmList germplasmList = this.germplasmListService.getGermplasmListById(Integer.parseInt(folderId)).get();
		if (!germplasmList.isFolder()) {
			this.errors.reject("list.folder.id.not.exist", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		//Validate if there is a folder with same name in parent folder
		this.validateNotSameFolderNameInParent(newFolderName, germplasmList.getParentId(), programUUID);

		final WorkbenchUser createdBy = this.securityService.getCurrentlyLoggedInUser();
		return this.germplasmListService.updateGermplasmListFolder(createdBy.getUserid(), newFolderName, Integer.valueOf(folderId), programUUID);
	}

	@Override
	public Integer moveGermplasmListFolder(final String cropName, final String programUUID, final String folderId,
		final String newParentFolderId) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		if (StringUtils.isEmpty(folderId) ) {
			this.errors.reject("list.folder.id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (StringUtils.isEmpty(newParentFolderId) || newParentFolderId.equals(CROP_LISTS)) {
			this.errors.reject("list.parent.id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (folderId.equals(newParentFolderId)) {
			this.errors.reject("list.move.id.same.values", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateProgram(cropName, programUUID);

		this.validateNodeId(newParentFolderId, programUUID, ListNodeType.PARENT, false);
		this.validateNodeId(folderId, programUUID, ListNodeType.FOLDER, false);

		final GermplasmList germplasmListToMove = this.germplasmListService.getGermplasmListById(Integer.parseInt(folderId))
			.orElseThrow(() -> {
				this.errors.reject("list.folder.id.not.exist", "");
				return new ApiRequestValidationException(this.errors.getAllErrors());
			});

		if(this.isSourceItemHasChildren(Integer.parseInt(folderId), programUUID)) {
			this.errors.reject("list.move.folder.has.child", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final Integer parent = this.getFolderIdAsInteger(newParentFolderId);
		if (!Objects.isNull(parent)) {
			final GermplasmList parentFolder = this.germplasmListService.getGermplasmListById(parent)
				.orElseThrow(() -> {
					this.errors.reject("list.parent.id.not.exist", "");
					return new ApiRequestValidationException(this.errors.getAllErrors());
				});

			if (!parentFolder.isFolder()) {
				this.errors.reject("list.move.list.another.list.not.allowed", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}

		//Validate if there is a folder with same name in parent folder
		this.germplasmListService.getGermplasmListByParentAndName(germplasmListToMove.getName(), parent, programUUID)
			.ifPresent(germplasmList -> {
				this.errors.reject("list.folder.name.exists", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			});

		return this.germplasmListService.moveGermplasmListFolder(Integer.parseInt(folderId), parent,
			(newParentFolderId.equals(CROP_LISTS)) ? null : programUUID);
	}

	@Override
	public void deleteGermplasmListFolder(final String cropName, final String programUUID, final String folderId) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		if (StringUtils.isEmpty(folderId)) {
			this.errors.reject("list.folder.id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateFolderNotCropNorProgramList(folderId);
		this.validateProgram(cropName, programUUID);
		this.validateNodeId(folderId, programUUID, ListNodeType.FOLDER);

		final GermplasmList folder = this.germplasmListService.getGermplasmListById(Integer.parseInt(folderId))
			.orElseThrow(() -> {
				this.errors.reject("list.parent.id.not.exist", "");
				return new ApiRequestValidationException(this.errors.getAllErrors());
			});

		if (!folder.isFolder()) {
			this.errors.reject("list.delete.not.folder", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if(this.isSourceItemHasChildren(Integer.parseInt(folderId), programUUID)) {
			this.errors.reject("list.delete.folder.has.child", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final WorkbenchUser createdBy = this.securityService.getCurrentlyLoggedInUser();
		if (!folder.getUserId().equals(createdBy.getUserid())) {
			this.errors.reject("list.delete.not.owner", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.germplasmListService.deleteGermplasmListFolder(Integer.parseInt(folderId));
	}

	@Override
	public List<GermplasmListDto> getGermplasmLists(final Integer gid) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(this.errors, Collections.singletonList(gid));
		return this.germplasmListService.getGermplasmLists(gid);
	}

	private void validateProgram(final String cropName, final String programUUID) {
		if (!StringUtils.isEmpty(programUUID)) {
			this.programValidator.validate(new ProgramDTO(cropName, programUUID), this.errors);
			if (this.errors.hasErrors()) {
				throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
			}
		}
	}

	private void validateNodeId(final String nodeId, final String programUUID, final ListNodeType nodeType) {
		this.validateNodeId(nodeId, programUUID, nodeType, true);
	}

	private void validateNodeId(final String nodeId, final String programUUID, final ListNodeType nodeType, final boolean validateRequiredProgramUUID) {
		if (!Objects.isNull(nodeId) && !PROGRAM_LISTS.equals(nodeId) && !CROP_LISTS.equals(nodeId) && !Util.isPositiveInteger(nodeId)) {
			this.errors.reject("list." + nodeType.getValue() + ".id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if ((PROGRAM_LISTS.equals(nodeId) && StringUtils.isEmpty(programUUID))) {
			this.errors.reject("list.project.mandatory", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (Util.isPositiveInteger(nodeId)) {
			final GermplasmList germplasmList =
				this.germplasmListService.getGermplasmListByIdAndProgramUUID(Integer.parseInt(nodeId), programUUID)
					.orElseThrow(() -> {
						this.errors.reject("list." + nodeType.getValue() + ".id.not.exist", "");
						return new ApiRequestValidationException(this.errors.getAllErrors());
					});

			if (validateRequiredProgramUUID && !StringUtils.isEmpty(programUUID) && StringUtils.isEmpty(germplasmList.getProgramUUID())) {
				this.errors.reject("list.project.mandatory", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateListName(final String currentProgram, final String name) {
		checkNotNull(name, "param.null", new String[] {"name"});
		checkArgument(name.length() <= 50, "text.field.max.length", new String[] {"name", "50"});
		if (AppConstants.CROP_LISTS.getString().equals(name)) {
			throw new ApiValidationException("", "error.list.name.invalid", AppConstants.CROP_LISTS.getString());
		}
		if (AppConstants.PROGRAM_LISTS.getString().equals(name)) {
			throw new ApiValidationException("", "error.list.name.invalid", AppConstants.PROGRAM_LISTS.getString());
		}
		final List<GermplasmList> germplasmListByName = this.germplasmListManager.getGermplasmListByName(name, currentProgram, 0, 1, Operation.EQUAL);
		if (!germplasmListByName.isEmpty()) {
			throw new ApiValidationException("", "error.list.name.exists");
		}
	}

	private boolean isSourceItemHasChildren(final Integer nodeId, final String programUUID) {
		final List<GermplasmList> listChildren = this.germplasmListManager
			.getGermplasmListByParentFolderId(nodeId, programUUID);
		return !listChildren.isEmpty();
	}

	private void validateNotSameFolderNameInParent(final String folderName, final Integer parent, final String programUUID) {
		this.germplasmListService.getGermplasmListByParentAndName(folderName, parent, programUUID)
			.ifPresent(germplasmList -> {
				this.errors.reject("list.folder.name.exists", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			});
	}

	private void validateFolderName(final String folderName) {
		if (StringUtils.isEmpty(folderName)) {
			this.errors.reject("list.folder.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (folderName.length() > NAME_MAX_LENGTH) {
			this.errors.reject("list.folder.name.too.long", new Object[] { NAME_MAX_LENGTH }, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateFolderNotCropNorProgramList(final String folderId) {
		if (folderId.equals(CROP_LISTS) || folderId.equals(PROGRAM_LISTS)) {
			this.errors.reject("list.folder.id.invalid", "");
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
