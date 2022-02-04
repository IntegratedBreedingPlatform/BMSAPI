package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.commons.workbook.generator.RowColumnType;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListObservationDto;
import org.generationcp.middleware.api.germplasmlist.MyListsDTO;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataService;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchRequest;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchResponse;
import org.generationcp.middleware.api.ontology.OntologyVariableService;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserProgramStateDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListMetadata;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.generationcp.middleware.util.VariableValueUtil;
import org.ibp.api.Util;
import org.ibp.api.domain.germplasmlist.GermplasmListMapper;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmListService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListDataValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.rest.common.UserTreeState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Transactional
public class GermplasmListServiceImpl implements GermplasmListService {

	public static final String PROGRAM_LISTS = "LISTS";
	public static final String CROP_LISTS = "CROPLISTS";
	public static final int BATCH_SIZE = 500;

	private static final String LEAD_CLASS = "lead";

	// List entry params
	static final String ENTRY_CODE = "entryCode";
	static final String SEED_SOURCE = "seedSource";
	static final String GROUP_NAME = "groupName";
	public static final String LIST_FIELD_UPDATE_NOT_SUPPORTED = "list.field.update.not.supported";
	public static final String LIST_FOLDER_ID_INVALID = "list.folder.id.invalid";
	public static final String ERROR_GERMPLASMLIST_SAVE_GAPS = "error.germplasmlist.save.gaps";
	public static final String ADMIN = "ADMIN";


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

	@Autowired
	private GermplasmListValidator germplasmListValidator;

	@Autowired
	private GermplasmListDataValidator germplasmListDataValidator;

	@Autowired
	private OntologyVariableService ontologyVariableService;

	@Autowired
	private GermplasmListDataService germplasmListDataService;

	private BindingResult errors;

	@Override
	public List<TreeNode> getGermplasmListChildrenNodes(final String crop, final String programUUID, final String parentId,
		final Boolean folderOnly) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		this.validateProgram(crop, programUUID);
		return this.getChildrenNodes(programUUID, parentId, folderOnly);
	}

	private List<TreeNode> getChildrenNodes(final String programUUID, final String parentId, final Boolean folderOnly) {

		this.validateNodeIdAcceptingCropFolders(parentId, programUUID, ListNodeType.FOLDER);

		checkNotNull(folderOnly, "list.folder.only");

		final List<TreeNode> treeNodes = new ArrayList<>();
		if (parentId == null) {
			final TreeNode cropFolderNode =
				new TreeNode(GermplasmListServiceImpl.CROP_LISTS, AppConstants.CROP_LISTS.getString(), true, LEAD_CLASS,
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
				rootLists = this.germplasmListManager.getGermplasmListByParentFolderIdBatched(Integer.parseInt(parentId), programUUID,
					GermplasmListServiceImpl.BATCH_SIZE);
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
		// Retrieve the list of expanded nodes
		final List<String> treeFolders = this.userProgramStateDataManager
			.getUserProgramTreeState(Integer.parseInt(userId), programUUID, ListTreeState.GERMPLASM_LIST.name());
		if (!isEmpty(treeFolders)) {
			final Map<String, TreeNode> folderParentNodeMap = new HashMap<>();
			final TreeNode programRootNode = treeNodesList.get(1);
			programRootNode.setChildren(this.getChildrenNodes(programUUID, programRootNode.getKey(), false));
			programRootNode.getChildren().forEach(c -> folderParentNodeMap.put(c.getKey(), programRootNode));
			for (int i = 1; i < treeFolders.size(); i++) {
				final String finalKey = StringUtils.stripStart(treeFolders.get(i), " ");
				final Optional<Map.Entry<String, TreeNode>> parentNodeOpt =
					folderParentNodeMap.entrySet().stream().filter(entry -> entry.getKey().equals(finalKey)).findFirst();
				// Find parent node then look for the node to expand among the parent's children then finally expand that node
				if (parentNodeOpt.isPresent()) {
					final TreeNode parentNode = parentNodeOpt.get().getValue();
					final Optional<TreeNode> nodeToExpand =
						parentNode.getChildren().stream().filter(child -> child.getKey().equals(finalKey)).findFirst();
					nodeToExpand.ifPresent(node -> {
							node.setChildren(this.getChildrenNodes(programUUID, finalKey, false));
							node.getChildren().forEach(c -> folderParentNodeMap.put(c.getKey(), node));
						}
					);
				}
			}

		}

		return treeNodesList;
	}

	@Override
	public void saveGermplasmListTreeState(final String crop, final String programUUID, final UserTreeState userTreeState) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		checkNotNull(userTreeState, "param.null", new String[] {"treeState"});
		this.validateProgram(crop, programUUID);

		final String userId = userTreeState.getUserId();
		this.userValidator.validateUserId(this.errors, userId);

		final List<String> folders = userTreeState.getFolders();
		this.validateFolders(folders, programUUID);
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		// Persist the tree state for user
		this.userProgramStateDataManager.saveOrUpdateUserProgramTreeState(Integer.parseInt(userId), programUUID,
			ListTreeState.GERMPLASM_LIST.name(),
			folders);
	}

	private void validateFolders(final List<String> folders, final String programUUID) {
		if (isEmpty(folders)) {
			this.errors.reject("list.folders.empty", "");
		}
		folders.forEach(nodeId -> this.validateNodeIdAcceptingCropFolders(nodeId.toUpperCase(), programUUID, ListNodeType.PARENT));
	}

	@Deprecated
	@Override
	public GermplasmList getGermplasmList(final Integer germplasmListId) {
		return this.germplasmListManager.getGermplasmListById(germplasmListId);
	}

	@Override
	public long countMyLists(final String programUUID, final Integer userId) {
		return this.germplasmListService.countMyLists(programUUID, userId);
	}

	@Override
	public List<MyListsDTO> getMyLists(final String programUUID, final Pageable pageable, final Integer userId) {
		final Map<String, GermplasmListTypeDTO> listTypes = this.getGermplasmListTypes()
			.stream().collect(toMap(GermplasmListTypeDTO::getCode, identity()));

		final List<MyListsDTO> myLists = this.germplasmListService.getMyLists(programUUID, pageable, userId);
		for (final MyListsDTO myList : myLists) {
			if (myList.getFolder() == null) {
				if (programUUID == null) {
					myList.setFolder(AppConstants.CROP_LISTS.getString());
				} else {
					myList.setFolder(AppConstants.PROGRAM_LISTS.getString());
				}
			}
			final GermplasmListTypeDTO type = listTypes.get(myList.getType());
			if (type != null) {
				myList.setTypeName(type.getName());
			}
		}
		return myLists;
	}

	@Override
	public GermplasmListDto clone(final Integer germplasmListId, final GermplasmListDto request) {
		final String currentProgram = ContextHolder.getCurrentProgram();

		this.germplasmListValidator.validateGermplasmList(germplasmListId);
		this.germplasmListValidator.validateListMetadata(request, currentProgram);
		this.germplasmListValidator.validateParentFolder(request);
		final Optional<GermplasmList> parentFolder = this.validateNodeIdAcceptingCropFolders(request.getParentFolderId(), currentProgram, ListNodeType.PARENT);

		this.assignFolderDependentProperties(request, currentProgram, parentFolder);

		return this.germplasmListService.cloneGermplasmList(germplasmListId, request,
			this.securityService.getCurrentlyLoggedInUser().getUserid());
	}

	@Override
	public GermplasmListGeneratorDTO create(final GermplasmListGeneratorDTO request) {

		final String currentProgram = ContextHolder.getCurrentProgram();

		final GermplasmListDto germplasmListDto = new GermplasmListDto(request);
		this.germplasmListValidator.validateListMetadata(germplasmListDto, currentProgram);

		this.germplasmListValidator.validateParentFolder(germplasmListDto);
		final Optional<GermplasmList> parentFolder = this.validateNodeIdAcceptingCropFolders(germplasmListDto.getParentFolderId(), currentProgram, ListNodeType.PARENT);

		// process and assign defaults + more validations
		this.processEntries(request, currentProgram);

		this.assignFolderDependentProperties(germplasmListDto, currentProgram, parentFolder);
		// set updated listdto fields to request for now, listdto and generatorlistdto to merge in the future

		//FIXME No sense to call a method to resolve values and reassign them to the request, split assignFolderDependentProperties
		//in 3 functions
		request.setParentFolderId(germplasmListDto.getParentFolderId());
		request.setProgramUUID(germplasmListDto.getProgramUUID());
		request.setStatus(germplasmListDto.getStatus());

		final Integer loggedInUser = this.securityService.getCurrentlyLoggedInUser().getUserid();

		// finally save
		return this.germplasmListService.create(request, loggedInUser);
	}

	private void assignFolderDependentProperties(final GermplasmListDto request, final String currentProgram, final Optional<GermplasmList> parentFolderOptional) {

		final String parentFolderId = request.getParentFolderId();

		if (CROP_LISTS.equals(parentFolderId) || (parentFolderOptional.isPresent() && StringUtils.isEmpty(parentFolderOptional.get()
			.getProgramUUID()))) {
			request.setProgramUUID(null);
		} else {
			request.setProgramUUID(currentProgram);
		}

		request.setStatus((StringUtils.isEmpty(request.getProgramUUID())) ? GermplasmList.Status.LOCKED_LIST.getCode() :
			GermplasmList.Status.LIST.getCode());

		if (CROP_LISTS.equals(parentFolderId) || PROGRAM_LISTS.equals(parentFolderId)) {
			request.setParentFolderId(null);
		}
	}

	@Override
	public void importUpdates(final GermplasmListGeneratorDTO request) {
		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(request.getId());
		this.germplasmListValidator.validateListIsUnlocked(germplasmList);

		this.processEntriesForUpdate(request);
		this.germplasmListService.importUpdates(request);
	}

	private void processEntries(final GermplasmListGeneratorDTO request, final String currentProgram) {

		// resolve/validate composite and entries

		final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite = request.getSearchComposite();
		checkArgument(!isEmpty(request.getEntries()) || searchComposite != null && searchComposite.isValid(),
			"error.germplasmlist.save.entries.or.composite");

		if (isEmpty(request.getEntries())) {
			if (!isEmpty(searchComposite.getItemIds())) {
				final List<Integer> gidsList = new ArrayList<Integer>(searchComposite.getItemIds());
				Collections.sort(gidsList);

				request.setEntries(gidsList.stream().map(gid -> {
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

		final List<Integer> gids = request.getEntries().stream().map(GermplasmListGeneratorDTO.GermplasmEntryDTO::getGid)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		checkArgument(!gids.isEmpty(), "error.germplasmlist.save.gid");
		final Map<Integer, Germplasm> germplasmMap = this.germplasmDataManager.getGermplasms(gids)
			.stream().collect(toMap(Germplasm::getGid, identity()));
		final Map<Integer, String> crossExpansions =
			this.pedigreeService.getCrossExpansionsBulk(new HashSet<>(gids), null, this.crossExpansionProperties);
		final Map<Integer, String> plotCodeValuesByGIDs = this.germplasmService.getPlotCodeValues(new HashSet<>(gids));

		final Map<Integer, Variable> entryDetailVariablesById = this.extractVariableIds(request);

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
				if (entry.getEntryCode().length() > 47) {
					throw new ApiValidationException("", "error.germplasmlist.save.entry.code.exceed.length");
				}
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

			this.processEntryDetails(entry.getData(), entryDetailVariablesById);
		}
		if (hasEntryNo && entryNo > 1) {
			throw new ApiValidationException("", "error.germplasmlist.save.entryno.gaps");
		}
		if (hasEntryCode && hasEntryCodeEmpty) {
			throw new ApiValidationException("", ERROR_GERMPLASMLIST_SAVE_GAPS, ENTRY_CODE);
		}
		if (hasSeedSource && hasSeedSourceEmpty) {
			throw new ApiValidationException("", ERROR_GERMPLASMLIST_SAVE_GAPS, SEED_SOURCE);
		}
		if (hasGroupName && hasGroupNameEmpty) {
			throw new ApiValidationException("", ERROR_GERMPLASMLIST_SAVE_GAPS, GROUP_NAME);
		}

	}

	private void processEntriesForUpdate(final GermplasmListGeneratorDTO request) {

		BaseValidator.checkNotNull(request.getId(), "error.germplasmlist.importupdates.listid.mandatory");

		final Map<Integer, Variable> entryDetailVariablesById = this.extractVariableIds(request);

		final int numberOfEntries =
			(int) this.germplasmListDataService.countSearchGermplasmListData(request.getId(), new GermplasmListDataSearchRequest());
		for (final GermplasmListGeneratorDTO.GermplasmEntryDTO entry : request.getEntries()) {
			if (entry.getEntryNo() == null) {
				throw new ApiValidationException("", "error.germplasmlist.importupdates.entryno.mandatory");
			}

			if (entry.getEntryNo() > numberOfEntries || entry.getEntryNo() < 1) {
				throw new ApiRequestValidationException("invalid.entry.no.value", new String[] {entry.getEntryNo().toString()});
			}

			// Temporary workaround to allow users to edit ENTRY_CODE
			if (!isBlank(entry.getEntryCode())) {
				if (entry.getEntryCode().length() > 47) {
					throw new ApiValidationException("", "error.germplasmlist.save.entry.code.exceed.length");
				}
			}

			this.processEntryDetails(entry.getData(), entryDetailVariablesById);
		}
	}

	private Map<Integer, Variable> extractVariableIds(final GermplasmListGeneratorDTO request) {
		final List<Integer> entryDetailVariableIds = request.getEntries().stream()
			.flatMap(germplasmEntryDTO -> germplasmEntryDTO.getData().keySet().stream())
			.collect(Collectors.toList());

		final VariableFilter filter = new VariableFilter();
		entryDetailVariableIds.forEach(filter::addVariableId);
		return !isEmpty(entryDetailVariableIds)
			? this.ontologyVariableService.getVariablesWithFilterById(filter)
			.entrySet().stream().collect(toMap(entry -> entry.getValue().getId(), Map.Entry::getValue))
			: Collections.emptyMap();
	}

	private void processEntryDetails(
		final Map<Integer, GermplasmListObservationDto> data,
		final Map<Integer, Variable> entryDetailVariablesById
	) {
		// complete variable info based on request
		for (final Map.Entry<Integer, GermplasmListObservationDto> entryDetailSet : data.entrySet()) {

			final GermplasmListObservationDto entryDetail = entryDetailSet.getValue();
			final Variable variable = entryDetailVariablesById.get(entryDetailSet.getKey());
			BaseValidator.checkNotNull(variable, "germplasm.list.variable.does.not.exist");
			final String value = entryDetail.getValue();
			this.validateVariableDataTypeValue(variable, value);

			entryDetail.setVariableId(variable.getId());
			entryDetail.setcValueId(VariableValueUtil.resolveCategoricalValueId(variable, value));
		}
	}

	private void validateVariableDataTypeValue(final Variable variable, final String value) {
		if (!VariableValueUtil.isValidAttributeValue(variable, value)) {
			throw new ApiRequestValidationException("invalid.variable.value.with.param", new String[] {
				"variable: " + variable.getName() + ", value: " + value
			});
		}
		checkArgument(value.length() <= 255, "text.field.max.length", new String[] {"entry detail value", "255"});
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

		this.searchCompositeDtoValidator.validateSearchCompositeDto(searchComposite, this.errors);
		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(germplasmListId);

		this.germplasmListValidator.validateListIsNotAFolder(germplasmList);
		this.germplasmListValidator.validateListIsUnlocked(germplasmList);

		if (!isEmpty(searchComposite.getItemIds())) {
			this.germplasmValidator.validateGids(this.errors, new ArrayList<>(searchComposite.getItemIds()));
		}

		this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, programUUID);
	}

	@Override
	public Integer createGermplasmListFolder(final String cropName, final String programUUID, final String folderName,
		final String parentId) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		this.germplasmListValidator.validateFolderName(folderName);
		this.validateProgram(cropName, programUUID);
		this.validateNodeIdAcceptingCropFolders(parentId, programUUID, ListNodeType.PARENT);

		//Validate if there is a folder with same name in parent folder
		final Integer parent = this.getFolderIdAsInteger(parentId);
		this.germplasmListValidator.validateNotSameFolderNameInParent(folderName, parent, programUUID);

		final WorkbenchUser createdBy = this.securityService.getCurrentlyLoggedInUser();
		return this.germplasmListService.createGermplasmListFolder(createdBy.getUserid(), folderName, parent, programUUID);
	}

	@Override
	public Integer updateGermplasmListFolderName(final String cropName, final String programUUID, final String newFolderName,
		final String folderId) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		this.validateFolderNotCropNorProgramList(folderId);
		this.germplasmListValidator.validateFolderName(newFolderName);
		this.validateProgram(cropName, programUUID);
		final GermplasmList germplasmList = this.validateNodeIdAcceptingCropFolders(folderId, programUUID, ListNodeType.FOLDER).get();

		if (!germplasmList.isFolder()) {
			this.errors.reject("list.folder.id.not.exist", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		//Preventing edition using the same list name
		if (newFolderName.equalsIgnoreCase(germplasmList.getName())) {
			return germplasmList.getId();
		}

		//Validate if there is a folder with same name in parent folder
		this.germplasmListValidator.validateNotSameFolderNameInParent(newFolderName, germplasmList.getParentId(), germplasmList.getProgramUUID());

		return this.germplasmListService.updateGermplasmListFolder(newFolderName, Integer.valueOf(folderId));
	}

	@Override
	public Integer moveGermplasmListFolder(final String cropName, final String programUUID, final String folderId,
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

		final Optional<GermplasmList> parentFolderOptional = this.validateNodeIdAcceptingCropFolders(newParentFolderId, programUUID, ListNodeType.PARENT);
		this.validateNodeId(folderId, ListNodeType.FOLDER);
		this.validateFolderNotCropNorProgramList(folderId);

		final GermplasmList germplasmListToMove = this.germplasmListService.getGermplasmListById(Integer.parseInt(folderId))
			.orElseThrow(() -> {
				this.errors.reject("list.folder.id.not.exist", "");
				return new ApiRequestValidationException(this.errors.getAllErrors());
			});

		this.getGermplasmListByIdAndProgramUUID(folderId, germplasmListToMove.getProgramUUID(), ListNodeType.FOLDER);

		if (this.isSourceItemHasChildren(Integer.parseInt(folderId), programUUID)) {
			this.errors.reject("list.move.folder.has.child", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final Integer parent = this.getFolderIdAsInteger(newParentFolderId);
		if (!Objects.isNull(parent)) {
			final GermplasmList parentFolder = parentFolderOptional.get();

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
			this.errors.reject(LIST_FOLDER_ID_INVALID, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateFolderNotCropNorProgramList(folderId);
		this.validateProgram(cropName, programUUID);
		final Optional<GermplasmList> germplasmList = this.validateNodeIdAcceptingCropFolders(folderId, programUUID, ListNodeType.FOLDER);
		final GermplasmList folder = germplasmList.get();

		if (!folder.isFolder()) {
			this.errors.reject("list.delete.not.folder", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (this.isSourceItemHasChildren(Integer.parseInt(folderId), programUUID)) {
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
	public GermplasmListDto getGermplasmListById(final Integer listId) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(listId);
		return GermplasmListMapper.getInstance().map(germplasmList, GermplasmListDto.class);
	}

	@Override
	public List<GermplasmListDto> getGermplasmLists(final Integer gid) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(this.errors, Collections.singletonList(gid));
		return this.germplasmListService.getGermplasmLists(gid);
	}

	@Override
	public List<GermplasmListSearchResponse> searchGermplasmList(final GermplasmListSearchRequest request,
		final Pageable pageable, final String programUUID) {
		return this.germplasmListService.searchGermplasmList(request, pageable, programUUID);
	}

	@Override
	public long countSearchGermplasmList(final GermplasmListSearchRequest request, final String programUUID) {
		return this.germplasmListService.countSearchGermplasmList(request, programUUID);
	}

	@Override
	public boolean toggleGermplasmListStatus(final Integer listId) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(listId);

		final WorkbenchUser createdBy = this.securityService.getCurrentlyLoggedInUser();
		final Collection<? extends GrantedAuthority> authorities = SecurityUtil.getLoggedInUserAuthorities();
		// Allow updating of status if user has Full permission or user owns the list
		if (authorities.stream().noneMatch( o ->
			Arrays.asList(PermissionsEnum.ADMIN.name(),
				PermissionsEnum.LISTS.name(),
				PermissionsEnum.MANAGE_GERMPLASM_LISTS.name(),
				PermissionsEnum.LOCK_UNLOCK_GERMPLASM_LIST.name()
			).contains(o.getAuthority()))
			&& !germplasmList.getUserId()
			.equals(createdBy.getUserid())) {
			this.errors.reject("list.toggle.status.not.owner", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		return this.germplasmListService.toggleGermplasmListStatus(listId);
	}

	@Override
	public void editListMetadata(final GermplasmListDto germplasmListDto, final String programUUID) {
		BaseValidator.checkNotNull(germplasmListDto.getListId(), "list.id.required");
		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(germplasmListDto.getListId());
		this.germplasmListValidator.validateListIsNotAFolder(germplasmList);
		this.germplasmListValidator.validateListIsUnlocked(germplasmList);

		this.germplasmListValidator.validateListMetadata(germplasmListDto, programUUID);

		// Throw exception for fields not supported for updating
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.validateListNonEditableFields(germplasmListDto, germplasmList);
		this.germplasmListService.editListMetadata(germplasmListDto);
	}

	private void validateListNonEditableFields(final GermplasmListDto germplasmListDto, final GermplasmList germplasmList) {
		if (germplasmListDto.getOwnerId() != null && !germplasmListDto.getOwnerId().equals(germplasmList.getUserId())) {
			this.errors.reject(LIST_FIELD_UPDATE_NOT_SUPPORTED, new String[] {"ownerId"}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		if (germplasmListDto.isLocked() != null && !germplasmListDto.isLocked().equals(germplasmList.isLockedList())) {
			this.errors.reject(LIST_FIELD_UPDATE_NOT_SUPPORTED, new String[] {"locked"}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		if (StringUtils.isNotBlank(germplasmListDto.getProgramUUID()) && !germplasmListDto.getProgramUUID()
			.equals(germplasmList.getProgramUUID())) {
			this.errors.reject(LIST_FIELD_UPDATE_NOT_SUPPORTED, new String[] {"programUUID"}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		if (germplasmListDto.getParentFolderId() != null && !Integer.valueOf(germplasmListDto.getParentFolderId())
			.equals(germplasmList.getParentId())) {
			this.errors.reject(LIST_FIELD_UPDATE_NOT_SUPPORTED, new String[] {"parentFolderId"}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		if (germplasmListDto.getStatus() != null && !germplasmListDto.getStatus().equals(germplasmList.getStatus())) {
			this.errors.reject(LIST_FIELD_UPDATE_NOT_SUPPORTED, new String[] {"status"}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	@Override
	public void removeGermplasmEntriesFromList(final Integer germplasmListId,
		final SearchCompositeDto<GermplasmListDataSearchRequest, Integer> searchComposite) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(germplasmListId);
		this.germplasmListValidator.validateListIsNotAFolder(germplasmList);
		this.germplasmListValidator.validateListIsUnlocked(germplasmList);
		this.searchCompositeDtoValidator.validateSearchCompositeDto(searchComposite,
			new MapBindingResult(new HashMap<>(), String.class.getName()));
		if (CollectionUtils.isNotEmpty(searchComposite.getItemIds()) && searchComposite.getSearchRequest() == null) {
			this.germplasmListDataValidator.verifyListDataIdsExist(germplasmListId, new ArrayList<>(searchComposite.getItemIds()));
		}
		this.germplasmListService.removeGermplasmEntriesFromList(germplasmListId, searchComposite);
	}

	@Override
	public List<Variable> getGermplasmListVariables(final String programUUID, final Integer listId, final Integer variableTypeId) {
		return this.germplasmListService.getGermplasmListVariables(programUUID, listId, variableTypeId);
	}

	@Override
	public void deleteGermplasmList(final String cropName, final String programUUID, final Integer listId) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.validateProgram(cropName, programUUID);
		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(listId);

		final WorkbenchUser createdBy = this.securityService.getCurrentlyLoggedInUser();
		final Collection<? extends GrantedAuthority> authorities = SecurityUtil.getLoggedInUserAuthorities();
		// Allow updating of status if user has Full permission or user owns the list
		if (authorities.stream().noneMatch(o ->
			Arrays.asList(PermissionsEnum.ADMIN.name(),
				PermissionsEnum.LISTS.name(),
				PermissionsEnum.MANAGE_GERMPLASM_LISTS.name(),
				PermissionsEnum.DELETE_GERMPLASM_LIST.name()
			).contains(o.getAuthority()))
			&& !germplasmList.getUserId()
			.equals(createdBy.getUserid())) {
			this.errors.reject("list.delete.not.owner", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.germplasmListValidator.validateListIsUnlocked(germplasmList);
		this.germplasmListService.deleteGermplasmList(listId);
	}

	@Override
	public void addGermplasmListEntriesToAnotherList(final String cropName, final String programUUID, final Integer destinationListId,
		final Integer sourceListId, final SearchCompositeDto<GermplasmListDataSearchRequest, Integer> searchComposite) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.validateProgram(cropName, programUUID);
		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(destinationListId);
		this.germplasmListValidator.validateListIsUnlocked(germplasmList);
		this.germplasmListValidator.validateGermplasmList(sourceListId);
		this.searchCompositeDtoValidator.validateSearchCompositeDto(searchComposite, this.errors);
		this.germplasmListService.addGermplasmListEntriesToAnotherList(destinationListId, sourceListId, programUUID, searchComposite);
	}

	private void validateProgram(final String cropName, final String programUUID) {
		if (!StringUtils.isEmpty(programUUID)) {
			this.programValidator.validate(new ProgramDTO(cropName, programUUID), this.errors);
			if (this.errors.hasErrors()) {
				throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
			}
		}
	}

	private Optional<GermplasmList> validateNodeIdAcceptingCropFolders(final String nodeId, final String programUUID, final ListNodeType nodeType) {

		this.validateNodeId(nodeId, nodeType);

		if (Util.isPositiveInteger(nodeId)) {

			final GermplasmList germplasmList = this.germplasmListService.getGermplasmListById(Integer.parseInt(nodeId))
				.orElseThrow(() -> {
					this.errors.reject("list.folder.id.not.exist", "");
					return new ApiRequestValidationException(this.errors.getAllErrors());
				});

			//verify that folder belongs to the program when it is not a crop folder
			if (!StringUtils.isEmpty(germplasmList.getProgramUUID())) {
				if (StringUtils.isEmpty(programUUID) || !programUUID.equals(germplasmList.getProgramUUID())) {
					this.errors.reject("list.project.mandatory", "");
					throw new ApiRequestValidationException(this.errors.getAllErrors());
				}
			}

			return Optional.of(germplasmList);
		}

		return Optional.empty();
	}

	private void validateNodeId(final String nodeId, final ListNodeType nodeType) {
		if (!Objects.isNull(nodeId) && !PROGRAM_LISTS.equals(nodeId) && !CROP_LISTS.equals(nodeId) && !Util.isPositiveInteger(nodeId)) {
			this.errors.reject("list." + nodeType.getValue() + ".id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private boolean isSourceItemHasChildren(final Integer nodeId, final String programUUID) {
		final List<GermplasmList> listChildren = this.germplasmListManager
			.getGermplasmListByParentFolderId(nodeId, programUUID);
		return !listChildren.isEmpty();
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

	private GermplasmList getGermplasmListByIdAndProgramUUID(final String nodeId, final String programUUID, final ListNodeType nodeType) {
		return this.germplasmListService.getGermplasmListByIdAndProgramUUID(Integer.parseInt(nodeId), programUUID)
			.orElseThrow(() -> {
				this.errors.reject("list." + nodeType.getValue() + ".id.not.exist", "");
				return new ApiRequestValidationException(this.errors.getAllErrors());
			});
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
