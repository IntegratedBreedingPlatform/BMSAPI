package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.security.SecurityUtil;
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
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserProgramStateDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.generationcp.middleware.util.VariableValueUtil;
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
import org.ibp.api.java.impl.middleware.security.SecurityService;
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
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Transactional
public class GermplasmListServiceImpl implements GermplasmListService {

	// List entry params
	static final String ENTRY_CODE = "entryCode";
	static final String SEED_SOURCE = "seedSource";
	static final String GROUP_NAME = "groupName";
	public static final String LIST_FIELD_UPDATE_NOT_SUPPORTED = "list.field.update.not.supported";
	public static final String LIST_FOLDER_ID_INVALID = "list.folder.id.invalid";
	public static final String ERROR_GERMPLASMLIST_SAVE_GAPS = "error.germplasmlist.save.gaps";
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
	private GermplasmListValidator germplasmListValidator;

	@Autowired
	private GermplasmListDataValidator germplasmListDataValidator;

	@Autowired
	private OntologyVariableService ontologyVariableService;

	@Autowired
	private GermplasmListDataService germplasmListDataService;

	private BindingResult errors;

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
		this.germplasmListValidator.validateParentFolder(request.getParentFolderId());
		final Optional<GermplasmList> parentFolder = this.germplasmListValidator.validateFolderId(request.getParentFolderId(), currentProgram, GermplasmListValidator.ListNodeType.PARENT);

		GermplasmListHelper.assignFolderDependentProperties(request, currentProgram, parentFolder);

		return this.germplasmListService.cloneGermplasmList(germplasmListId, request,
			this.securityService.getCurrentlyLoggedInUser().getUserid());
	}

	@Override
	public GermplasmListGeneratorDTO create(final GermplasmListGeneratorDTO request) {

		final String currentProgram = ContextHolder.getCurrentProgram();

		this.germplasmListValidator.validateParentFolder(request.getParentFolderId());
		final Optional<GermplasmList> parentFolder = this.germplasmListValidator.validateFolderId(request.getParentFolderId(), currentProgram, GermplasmListValidator.ListNodeType.PARENT);

		GermplasmListHelper.assignFolderDependentProperties(request, currentProgram, parentFolder);

		//TODO Fixed to use final programUUID, but it should look for the name inside the directory instead
		this.germplasmListValidator.validateListMetadata(request, request.getProgramUUID());

		// process and assign defaults + more validations
		this.processEntries(request, currentProgram);

		final Integer loggedInUser = this.securityService.getCurrentlyLoggedInUser().getUserid();

		// finally save
		return this.germplasmListService.create(request, loggedInUser);
	}

	@Override
	public void importUpdates(final GermplasmListGeneratorDTO request) {
		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(request.getListId());
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
		} else {
			// Remove ENTRY_NO variable to avoid create the input in list_data_details table.
			// The value of ENTRY_NO is continued taken from listdata table.
			request.getEntries().stream().forEach(germplasmEntry -> {
				GermplasmListObservationDto germplasmListObservationDto = germplasmEntry.getData().get(TermId.ENTRY_NO.getId());
				if (germplasmListObservationDto != null) {
					germplasmEntry.getData().remove(TermId.ENTRY_NO.getId());
				}
			});
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
		if (hasSeedSource && hasSeedSourceEmpty) {
			throw new ApiValidationException("", ERROR_GERMPLASMLIST_SAVE_GAPS, SEED_SOURCE);
		}
		if (hasGroupName && hasGroupNameEmpty) {
			throw new ApiValidationException("", ERROR_GERMPLASMLIST_SAVE_GAPS, GROUP_NAME);
		}

	}

	private void processEntriesForUpdate(final GermplasmListGeneratorDTO request) {

		BaseValidator.checkNotNull(request.getListId(), "error.germplasmlist.importupdates.listid.mandatory");

		final Map<Integer, Variable> entryDetailVariablesById = this.extractVariableIds(request);

		final int numberOfEntries =
			(int) this.germplasmListDataService.countSearchGermplasmListData(request.getListId(), new GermplasmListDataSearchRequest());
		for (final GermplasmListGeneratorDTO.GermplasmEntryDTO entry : request.getEntries()) {
			if (entry.getEntryNo() == null) {
				throw new ApiValidationException("", "error.germplasmlist.importupdates.entryno.mandatory");
			}

			if (entry.getEntryNo() > numberOfEntries || entry.getEntryNo() < 1) {
				throw new ApiRequestValidationException("invalid.entry.no.value", new String[] {entry.getEntryNo().toString()});
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
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		BaseValidator.checkNotNull(germplasmListDto.getListId(), "list.id.required");
		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(germplasmListDto.getListId());

		if (!StringUtils.isEmpty(germplasmList.getProgramUUID()) && !StringUtils.isEmpty(programUUID) && !programUUID.equals(
			germplasmList.getProgramUUID())) {
			this.errors.reject("list.does.not.match.program", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.germplasmListValidator.validateListIsNotAFolder(germplasmList);
		this.germplasmListValidator.validateListIsUnlocked(germplasmList);

		this.germplasmListValidator.validateListMetadata(germplasmListDto, germplasmList.getProgramUUID());

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

		if (!StringUtils.isEmpty(germplasmList.getProgramUUID()) && !StringUtils.isEmpty(programUUID) && !programUUID.equals(
			germplasmList.getProgramUUID())) {
			this.errors.reject("list.does.not.match.program", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

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
