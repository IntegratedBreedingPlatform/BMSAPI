package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.commons.workbook.generator.RowColumnType;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.dao.GermplasmListDataDAO;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListMetadata;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.Util;
import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplamListService;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.splitByCharacterType;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Service
@Transactional
public class GermplamListServiceImpl implements GermplamListService {

	public static final String PROGRAM_LISTS = "LISTS";
	public static final String CROP_LISTS = "CROPLISTS";
	private static final String LEAD_CLASS = "lead";
	public static final int BATCH_SIZE = 500;

	// List entry params
	static final String ENTRY_CODE = "entryCode";
	static final String SEED_SOURCE = "seedSource";
	static final String DESIGNATION = "designation";
	static final String GROUP_NAME = "groupName";

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	public WorkbenchDataManager workbenchDataManager;

	@Autowired
	public GermplasmDataManager germplasmDataManager;

	@Autowired
	public GermplasmListService germplasmListService;

	@Autowired
	public ProgramValidator programValidator;

	@Autowired
	public SecurityService securityService;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	private BindingResult errors;

	@Override
	public List<TreeNode> getGermplasmListChildrenNodes(final String crop, final String programUUID, final String parentId,
		final Boolean folderOnly) {

		errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());

		if (!StringUtils.isEmpty(programUUID)) {
			this.programValidator.validate(new ProgramSummary(crop, programUUID), errors);
			if (errors.hasErrors()) {
				throw new ResourceNotFoundException(errors.getAllErrors().get(0));
			}
		}

		this.validateParentId(parentId, programUUID);
		checkNotNull(folderOnly, "list.folder.only");

		final List<TreeNode> treeNodes = new ArrayList<>();
		if (parentId == null) {
			final TreeNode cropFolderNode = new TreeNode(GermplamListServiceImpl.CROP_LISTS, AppConstants.CROP_LISTS.getString(), true, LEAD_CLASS,
					AppConstants.FOLDER_ICON_PNG.getString(), null);
			cropFolderNode.setNumOfChildren(this.germplasmListManager.getAllTopLevelLists(null).size());
			treeNodes.add(cropFolderNode);
			if (programUUID != null) {
				final TreeNode programFolderNode = new TreeNode(GermplamListServiceImpl.PROGRAM_LISTS, AppConstants.PROGRAM_LISTS.getString(), true, LEAD_CLASS,
						AppConstants.FOLDER_ICON_PNG.getString(), programUUID);
				programFolderNode.setNumOfChildren(this.germplasmListManager.getAllTopLevelLists(programUUID).size());
				treeNodes.add(programFolderNode);
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

			this.germplasmListManager.populateGermplasmListCreatedByName(rootLists);

			final List<UserDefinedField> listTypes = germplasmDataManager
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

	@Override
	public GermplasmList getGermplasmList(final Integer germplasmListId) {
		return this.germplasmListManager.getGermplasmListById(germplasmListId);
	}

	@Override
	public GermplasmListGeneratorDTO create(final GermplasmListGeneratorDTO request) {

		final String currentProgram = ContextHolder.getCurrentProgram();

		// validations

		checkNotNull(request, "param.null", new String[] {"request"});
		checkNotNull(request.getEntries(), "param.null", new String[] {"request entries"});
		checkArgument(!request.getEntries().isEmpty(), "param.null", new String[] {"request entries"});
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
		this.validateParentId(parentFolderId, currentProgram);

		// process and assign defaults + more validations
		this.processEntries(request.getEntries());

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

	private void processEntries(final List<GermplasmListGeneratorDTO.GermplasmEntryDTO> entries) {

		final List<Integer> gids = entries.stream().map(GermplasmListGeneratorDTO.GermplasmEntryDTO::getGid).collect(Collectors.toList());
		final Map<Integer, String> preferrredNamesMap = this.germplasmDataManager.getPreferredNamesByGids(gids);
		final Map<Integer, Germplasm> germplasmMap = this.germplasmDataManager.getGermplasms(gids)
			.stream().collect(Collectors.toMap(Germplasm::getGid, Function.identity()));

		int entryNo = 1;
		boolean hasEntryNo = false;
		boolean hasEntryCode = false;
		boolean hasEntryCodeEmpty = false;
		boolean hasSeedSource = false;
		boolean hasSeedSourceEmpty = false;
		boolean hasDesignation = false;
		boolean hasDesignationEmpty = false;
		boolean hasGroupName = false;
		boolean hasGroupNameEmpty = false;

		for (final GermplasmListGeneratorDTO.GermplasmEntryDTO entry : entries) {
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
				entry.setSeedSource(GermplasmListDataDAO.SOURCE_UNKNOWN);
				hasSeedSourceEmpty = true;
			} else {
				hasSeedSource = true;
			}

			if (isBlank(entry.getDesignation())) {
				entry.setDesignation(preferrredNamesMap.get(gid));
				hasDesignationEmpty = true;
			} else {
				hasDesignation = true;
			}

			if (isBlank(entry.getGroupName())) {
				final String crossExpansion = this.pedigreeService.getCrossExpansion(gid, this.crossExpansionProperties);
				entry.setGroupName(crossExpansion);
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
		if (hasDesignation && hasDesignationEmpty) {
			throw new ApiValidationException("", "error.germplasmlist.save.gaps", DESIGNATION);
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
				GermplasmListTypeDTO germplasmListTypeDTO = new GermplasmListTypeDTO();
				germplasmListTypeDTO.setCode(userDefinedField.getFcode());
				germplasmListTypeDTO.setId(userDefinedField.getFldno());
				germplasmListTypeDTO.setName(userDefinedField.getFname());
				return germplasmListTypeDTO;
			})
			.collect(Collectors.toList());
	}

	private void validateParentId(final String parentId, final String programUUID) {
		if (parentId != null && !PROGRAM_LISTS.equals(parentId) && !CROP_LISTS.equals(parentId) && !Util.isPositiveInteger(parentId)) {
			this.errors.reject("list.parent.id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if ((PROGRAM_LISTS.equals(parentId) || Util.isPositiveInteger(parentId)) && StringUtils.isEmpty(programUUID)) {
			this.errors.reject("list.project.mandatory", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (Util.isPositiveInteger(parentId) && !StringUtils.isEmpty(programUUID)) {
			final GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(Integer.parseInt(parentId));
			if (germplasmList == null || !germplasmList.isFolder()) {
				this.errors.reject("list.parent.id.not.exist", "");
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
