
package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.api.attribute.AttributeService;
import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.generationcp.middleware.api.germplasm.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.germplasm.DescendantTree;
import org.ibp.api.domain.germplasm.DescendantTreeTreeNode;
import org.ibp.api.domain.germplasm.GermplasmName;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.germplasm.PedigreeTree;
import org.ibp.api.domain.germplasm.PedigreeTreeNode;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmImportRequestDtoValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class GermplasmServiceImpl implements GermplasmService {

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private AttributeValidator attributeValidator;

	private BindingResult errors;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private PedigreeDataManager pedigreeDataManager;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	@Autowired
	private LocationDataManager locationDataManger;

	@Autowired
	private GermplasmGroupingService germplasmGroupingService;

	@Autowired
	private InstanceValidator instanceValidator;

	@Autowired
	private GermplasmSearchService germplasmSearchService;

	@Autowired
	private AttributeService attributeService;

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmService germplasmService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private GermplasmImportRequestDtoValidator germplasmImportRequestDtoValidator;

	@Override
	public List<GermplasmSearchResponse> searchGermplasm(final GermplasmSearchRequest germplasmSearchRequest, final Pageable pageable,
		final String programUUID) {

		final List<GermplasmSearchResponse> responseList =
			this.germplasmSearchService.searchGermplasm(germplasmSearchRequest, pageable, programUUID);

		if (responseList == null || responseList.isEmpty()) {
			return responseList;
		}

		final Map<Integer, GermplasmSearchResponse> responseMap
			= responseList.stream().collect(Collectors.toMap(GermplasmSearchResponse::getGid, Function.identity()));

		final Map<Integer, String> pedigreeStringMap =
			this.pedigreeService.getCrossExpansions(new HashSet<>(responseMap.keySet()), null, this.crossExpansionProperties);

		for (final Map.Entry<Integer, GermplasmSearchResponse> entry : responseMap.entrySet()) {
			final Integer gid = entry.getKey();
			final GermplasmSearchResponse response = entry.getValue();
			response.setPedigreeString(pedigreeStringMap.get(gid));
		}

		this.addParentsFromPedigreeTable(responseMap, germplasmSearchRequest);

		return responseList;
	}

	private void addParentsFromPedigreeTable(final Map<Integer, GermplasmSearchResponse> responseMap,
		final GermplasmSearchRequest germplasmSearchRequest) {

		final List<String> addedColumnsPropertyIds = germplasmSearchRequest.getAddedColumnsPropertyIds();

		if (addedColumnsPropertyIds == null || addedColumnsPropertyIds.isEmpty()
			|| !(addedColumnsPropertyIds.contains(ColumnLabels.FGID.getName())
			|| addedColumnsPropertyIds.contains(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName())
			|| addedColumnsPropertyIds.contains(ColumnLabels.MGID.getName())
			|| addedColumnsPropertyIds.contains(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()))) {
			return;
		}

		final Integer level = this.crossExpansionProperties.getCropGenerationLevel(this.pedigreeService.getCropName());
		/**
		 * TODO Investigate sql approach.
		 *  See {@link org.generationcp.middleware.dao.GermplasmSearchDAO#retrievePedigreeGids(List, GermplasmSearchRequest)}
		 *  -> 1000 results, 10 levels of pedigree => ~10 sec
		 *  pedigreeDataManager.generatePedigreeTable:
		 *  -> 1000 results, 1 level of pedigree => ~1 min
		 */
		final com.google.common.collect.Table<Integer, String, Optional<Germplasm>> pedigreeTreeNodeTable =
			this.pedigreeDataManager.generatePedigreeTable(responseMap.keySet(), level, false);

		for (final Map.Entry<Integer, GermplasmSearchResponse> entry : responseMap.entrySet()) {
			final Integer gid = entry.getKey();
			final GermplasmSearchResponse response = entry.getValue();

			final Optional<Germplasm> femaleParent = pedigreeTreeNodeTable.get(gid, ColumnLabels.FGID.getName());
			final Optional<Germplasm> maleParent = pedigreeTreeNodeTable.get(gid, ColumnLabels.MGID.getName());

			if (femaleParent.isPresent()) {
				final Germplasm germplasm = femaleParent.get();
				response.setFemaleParentGID(germplasm.getGid() != 0 ? String.valueOf(germplasm.getGid()) : Name.UNKNOWN);
				response.setFemaleParentPreferredName(germplasm.getPreferredName().getNval());
			}
			if (maleParent.isPresent()) {
				final Germplasm germplasm = maleParent.get();
				response.setMaleParentGID(germplasm.getGid() != 0 ? String.valueOf(germplasm.getGid()) : Name.UNKNOWN);
				response.setMaleParentPreferredName(germplasm.getPreferredName().getNval());
			}
		}
	}

	@Override
	public long countSearchGermplasm(final GermplasmSearchRequest germplasmSearchRequest, final String programUUID) {
		return this.germplasmSearchService.countSearchGermplasm(germplasmSearchRequest, programUUID);
	}

	/*
	 * TODO
	 *  - Remove
	 *  - GermplasmSearchResponse replaces GermplasmSummary
	 *  - find a substitute to use in getGermplasm(), which is used only for validation now
	 */
	@Deprecated
	private GermplasmSummary populateGermplasmSummary(final Germplasm germplasm) throws MiddlewareQueryException {
		if (germplasm == null) {
			return null;
		}
		final GermplasmSummary summary = new GermplasmSummary();
		summary.setGermplasmId(germplasm.getGid().toString());
		summary.setParent1Id(germplasm.getGpid1() != null && germplasm.getGpid1() != 0 ? germplasm.getGpid1().toString() : "Unknown");
		summary.setParent2Id(germplasm.getGpid2() != null && germplasm.getGpid2() != 0 ? germplasm.getGpid2().toString() : "Unknown");

		summary.setPedigreeString(this.pedigreeService.getCrossExpansion(germplasm.getGid(), this.crossExpansionProperties));

		// FIXME - select in a loop ... Middleware service should handle all this in main query.
		final List<Name> namesByGID = this.germplasmDataManager.getNamesByGID(new Integer(germplasm.getGid()), null, null);
		final List<GermplasmName> names = new ArrayList<GermplasmName>();
		for (final Name gpName : namesByGID) {
			final GermplasmName germplasmName = new GermplasmName();
			germplasmName.setName(gpName.getNval());
			final UserDefinedField nameType = this.germplasmDataManager.getUserDefinedFieldByID(gpName.getTypeId());
			if (nameType != null) {
				germplasmName.setNameTypeCode(nameType.getFcode());
				germplasmName.setNameTypeDescription(nameType.getFname());
			}
			names.add(germplasmName);
		}
		summary.addNames(names);

		final Method germplasmMethod = this.germplasmDataManager.getMethodByID(germplasm.getMethodId());
		if (germplasmMethod != null && germplasmMethod.getMname() != null) {
			summary.setBreedingMethod(germplasmMethod.getMname());
		}

		final Location germplasmLocation = this.locationDataManger.getLocationByID(germplasm.getLocationId());
		if (germplasmLocation != null && germplasmLocation.getLname() != null) {
			summary.setLocation(germplasmLocation.getLname());
		}
		return summary;
	}

	@Override
	public GermplasmSummary getGermplasm(final String germplasmId) {
		final Germplasm germplasm;
		try {
			germplasm = this.germplasmDataManager.getGermplasmByGID(Integer.valueOf(germplasmId));
			return this.populateGermplasmSummary(germplasm);
		} catch (final NumberFormatException | MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public List<org.generationcp.middleware.api.attribute.AttributeDTO> searchAttributes(final String query) {
		return this.attributeService.searchAttributes(query);
	}

	@Override
	public PedigreeDTO getPedigree(final Integer germplasmDbId, final String notation, final Boolean includeSiblings) {
		final PedigreeDTO pedigreeDTO;
		try {
			pedigreeDTO = this.germplasmDataManager.getPedigree(germplasmDbId, notation, includeSiblings);
			if (pedigreeDTO != null) {
				pedigreeDTO.setPedigree(this.pedigreeService.getCrossExpansion(germplasmDbId, this.crossExpansionProperties));
			}
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to get the pedigree", e);
		}
		return pedigreeDTO;
	}

	@Override
	public ProgenyDTO getProgeny(final Integer germplasmDbId) {
		final ProgenyDTO progenyDTO;
		try {
			progenyDTO = this.germplasmDataManager.getProgeny(germplasmDbId);
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to get the progeny", e);
		}
		return progenyDTO;
	}

	// TODO delete. See populateGermplasmSummary
	@Override
	public PedigreeTree getPedigreeTree(final String germplasmId, Integer levels) {

		if (levels == null) {
			levels = DEFAULT_PEDIGREE_LEVELS;
		}
		final GermplasmPedigreeTree mwTree = this.pedigreeDataManager.generatePedigreeTree(Integer.valueOf(germplasmId), levels);

		final PedigreeTree pedigreeTree = new PedigreeTree();
		pedigreeTree.setRoot(this.traversePopulate(mwTree.getRoot()));

		return pedigreeTree;
	}

	// TODO delete. See populateGermplasmSummary
	@Override
	public DescendantTree getDescendantTree(final String germplasmId) {
		final Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(Integer.valueOf(germplasmId));
		final GermplasmPedigreeTree mwTree = this.germplasmGroupingService.getDescendantTree(germplasm);

		final DescendantTree descendantTree = new DescendantTree();
		descendantTree.setRoot(this.traversePopulateDescendatTree(mwTree.getRoot()));

		return descendantTree;
	}

	// TODO delete. See populateGermplasmSummary
	private DescendantTreeTreeNode traversePopulateDescendatTree(final GermplasmPedigreeTreeNode mwTreeNode) {
		final DescendantTreeTreeNode treeNode = new DescendantTreeTreeNode();
		treeNode.setGermplasmId(mwTreeNode.getGermplasm().getGid());
		treeNode.setProgenitors(mwTreeNode.getGermplasm().getGnpgs());
		treeNode.setMethodId(mwTreeNode.getGermplasm().getMethodId());
		treeNode.setParent1Id(mwTreeNode.getGermplasm().getGpid1());
		treeNode.setParent2Id(mwTreeNode.getGermplasm().getGpid2());
		treeNode.setManagementGroupId(mwTreeNode.getGermplasm().getMgid());

		final Name preferredName = mwTreeNode.getGermplasm().findPreferredName();
		treeNode.setName(preferredName != null ? preferredName.getNval() : null);

		final List<DescendantTreeTreeNode> nodeChildren = new ArrayList<>();
		for (final GermplasmPedigreeTreeNode mwChild : mwTreeNode.getLinkedNodes()) {
			nodeChildren.add(this.traversePopulateDescendatTree(mwChild));
		}
		treeNode.setChildren(nodeChildren);
		return treeNode;
	}

	// TODO delete. See populateGermplasmSummary
	private PedigreeTreeNode traversePopulate(final GermplasmPedigreeTreeNode mwTreeNode) {
		final PedigreeTreeNode treeNode = new PedigreeTreeNode();
		treeNode.setGermplasmId(mwTreeNode.getGermplasm().getGid().toString());
		treeNode.setName(mwTreeNode.getGermplasm().getPreferredName() != null ? mwTreeNode.getGermplasm().getPreferredName().getNval()
				: null);

		final List<PedigreeTreeNode> nodeParents = new ArrayList<>();
		for (final GermplasmPedigreeTreeNode mwParent : mwTreeNode.getLinkedNodes()) {
			nodeParents.add(this.traversePopulate(mwParent));
		}
		treeNode.setParents(nodeParents);
		return treeNode;
	}

	@Override
	public int searchGermplasmCount(final String searchText) {

		final GermplasmSearchParameter searchParameter = new GermplasmSearchParameter(searchText, Operation.LIKE, false, false, false);

		return this.germplasmDataManager.countSearchForGermplasm(searchParameter);
	}

	@Override
	public GermplasmDTO getGermplasmDTObyGID (final Integer germplasmId) {
		final GermplasmDTO germplasmDTO;
		try {
			germplasmDTO = this.germplasmDataManager.getGermplasmDTOByGID(germplasmId);
			if (germplasmDTO != null) {
				germplasmDTO.setPedigree(this.pedigreeService.getCrossExpansion(germplasmId, this.crossExpansionProperties));
			}
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to get a germplasm", e);
		}
		return germplasmDTO;
	}

	@Override
	public List<GermplasmDTO> searchGermplasmDTO(
		final GermplasmSearchRequestDto germplasmSearchRequestDTO, final Integer page, final Integer pageSize) {
		try {

			final List<GermplasmDTO> germplasmDTOList = this.germplasmDataManager
				.searchGermplasmDTO(germplasmSearchRequestDTO, page, pageSize);
			if (germplasmDTOList != null) {
				this.populateGermplasmPedigreeAndSynonyms(germplasmDTOList);
			}
			return germplasmDTOList;
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to search germplasms", e);
		}
	}

	@Override
	public long countGermplasmDTOs(final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		try {
			return this.germplasmDataManager.countGermplasmDTOs(germplasmSearchRequestDTO);
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to count germplasms", e);
		}
	}

	@Override
	public long countGermplasmByStudy(final Integer studyDbId) {
		try {
			return this.germplasmDataManager.countGermplasmByStudy(studyDbId);
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to count germplasms", e);
		}
	}

	@Override
	public List<GermplasmDTO> getGermplasmByStudy(final int studyDbId, final int pageSize, final int pageNumber) {
		try {

			this.instanceValidator.validateStudyDbId(studyDbId);

			final List<GermplasmDTO> germplasmDTOList = this.germplasmDataManager
				.getGermplasmByStudy(studyDbId, pageNumber, pageSize);
			if (germplasmDTOList != null) {
				this.populateGermplasmPedigreeAndSynonyms(germplasmDTOList);
			}
			return germplasmDTOList;
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to search germplasms", e);
		}
	}

	private void populateGermplasmPedigreeAndSynonyms(final List<GermplasmDTO> germplasmDTOList) {
		final Set<Integer> gids = germplasmDTOList.stream().map(germplasmDTO -> Integer.valueOf(germplasmDTO.getGermplasmDbId()))
				.collect(Collectors.toSet());
		final Map<Integer, String> crossExpansionsMap =
				this.pedigreeService.getCrossExpansions(gids, null, this.crossExpansionProperties);
		final Map<Integer, List<Name>> gidNamesMap = this.germplasmDataManager.getNamesByGidsAndNTypeIdsInMap(new ArrayList<>(gids), Collections.emptyList());
		for (final GermplasmDTO germplasmDTO : germplasmDTOList) {
			final Integer gid = Integer.valueOf(germplasmDTO.getGermplasmDbId());
			// Set as synonyms other names, other than the preferred name, found for germplasm
			final String defaultName = germplasmDTO.getGermplasmName();
			final List<Name> names = gidNamesMap.get(gid);
			if (!CollectionUtils.isEmpty(names)){
				germplasmDTO.setSynonyms(names.stream().filter(n-> !defaultName.equalsIgnoreCase(n.getNval())).map(Name::getNval).collect(Collectors.toList()));
			}
			germplasmDTO.setPedigree(crossExpansionsMap.get(gid));
		}
	}

	@Override
	public List<AttributeDTO> getAttributesByGid(
		final String gid, final List<String> attributeDbIds, final Integer pageSize, final Integer pageNumber) {
		this.validateGidAndAttributes(gid, attributeDbIds);
		return this.germplasmDataManager.getAttributesByGid(gid, attributeDbIds, pageSize, pageNumber);
	}

	@Override
	public long countAttributesByGid(final String gid, final List<String> attributeDbIds) {
		return this.germplasmDataManager.countAttributesByGid(gid, attributeDbIds);
	}

	@Override
	public List<GermplasmNameTypeDTO> getGermplasmNameTypesByCodes(final Set<String> codes) {

		return this.germplasmDataManager.getUserDefinedFieldByTableTypeAndCodes(UDTableType.NAMES_NAME.getTable(),
			Collections.singleton(UDTableType.NAMES_NAME.getType()), codes)
			.stream()
			.map(userDefinedField -> {
				final GermplasmNameTypeDTO germplasmNameTypeDTO = new GermplasmNameTypeDTO();
				germplasmNameTypeDTO.setId(userDefinedField.getFldno());
				germplasmNameTypeDTO.setName(userDefinedField.getFname());
				germplasmNameTypeDTO.setCode(userDefinedField.getFcode());
				return germplasmNameTypeDTO;
			})
			.collect(Collectors.toList());
	}

	@Override
	public List<org.generationcp.middleware.api.attribute.AttributeDTO> getGermplasmAttributesByCodes(final Set<String> codes) {

		final Set<String> types = new HashSet<>();
		types.add(UDTableType.ATRIBUTS_ATTRIBUTE.getType());
		types.add(UDTableType.ATRIBUTS_PASSPORT.getType());
		return this.germplasmDataManager.getUserDefinedFieldByTableTypeAndCodes(UDTableType.ATRIBUTS_ATTRIBUTE.getTable(), types, codes)
			.stream()
			.map(userDefinedField -> {
				final org.generationcp.middleware.api.attribute.AttributeDTO attributeDTO =
					new org.generationcp.middleware.api.attribute.AttributeDTO();
				attributeDTO.setId(userDefinedField.getFldno());
				attributeDTO.setName(userDefinedField.getFname());
				attributeDTO.setCode(userDefinedField.getFcode());
				return attributeDTO;
			})
			.collect(Collectors.toList());
	}

	@Override
	public Map<Integer, Integer> importGermplasm(final String cropName, final String programUUID,
		final GermplasmImportRequestDto germplasmImportRequestDto) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
		germplasmImportRequestDtoValidator.validate(programUUID, germplasmImportRequestDto);
		return this.germplasmService.importGermplasm(user.getUserid(), cropName, germplasmImportRequestDto);
	}

	private void validateGidAndAttributes(final String gid, final List<String> attributeDbIds) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), AttributeDTO.class.getName());
		this.germplasmValidator.validateGermplasmId(this.errors, Integer.valueOf(gid));
		if (this.errors.hasErrors()) {
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
		this.attributeValidator.validateAttributeIds(this.errors, attributeDbIds);
		if (this.errors.hasErrors()) {
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
	}

	void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	void setPedigreeService(final PedigreeService pedigreeService) {
		this.pedigreeService = pedigreeService;
	}

	void setLocationDataManger(final LocationDataManager locationDataManger) {
		this.locationDataManger = locationDataManger;
	}

	void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

}
