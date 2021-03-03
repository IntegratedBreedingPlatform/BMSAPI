
package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.collections.CollectionUtils;
import org.generationcp.middleware.api.attribute.AttributeService;
import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportResponseDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmMatchRequestDto;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.brapi.v2.germplasm.GermplasmImportRequestValidator;
import org.ibp.api.brapi.v2.germplasm.GermplasmImportResponse;
import org.ibp.api.domain.germplasm.GermplasmDeleteResponse;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmDeleteValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmUpdateDtoValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmImportRequestDtoValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

	@Autowired
	private GermplasmUpdateDtoValidator germplasmUpdateDtoValidator;

	@Autowired
	private GermplasmDeleteValidator germplasmDeleteValidator;

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
	private InstanceValidator instanceValidator;

	@Autowired
	private GermplasmSearchService germplasmSearchService;

	@Autowired
	private AttributeService attributeService;

	@Autowired
	private GermplasmNameTypeService germplasmNameTypeService;

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmService germplasmService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private GermplasmImportRequestDtoValidator germplasmImportRequestDtoValidator;

	@Autowired
	private GermplasmImportRequestValidator germplasmImportValidator;


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

	@Override
	public List<org.generationcp.middleware.api.attribute.AttributeDTO> searchAttributes(final String query) {
		return this.attributeService.searchAttributes(query);
	}

	@Override
	public List<org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO> searchNameTypes(final String query) {
		return this.germplasmNameTypeService.searchNameTypes(query);
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

	@Override
	public int searchGermplasmCount(final String searchText) {

		final GermplasmSearchParameter searchParameter = new GermplasmSearchParameter(searchText, Operation.LIKE, false, false, false);

		return this.germplasmDataManager.countSearchForGermplasm(searchParameter);
	}

	@Override
	public GermplasmDTO getGermplasmDTObyGID(final Integer germplasmId) {
		final Optional<GermplasmDTO> germplasmDTOOptional = this.germplasmService.getGermplasmDTOByGID(germplasmId);
		if (germplasmDTOOptional.isPresent()) {
			final GermplasmDTO germplasmDTO = germplasmDTOOptional.get();
			germplasmDTO.setPedigree(this.pedigreeService.getCrossExpansion(germplasmId, this.crossExpansionProperties));
			return germplasmDTO;
		} else {
			throw new ApiRuntimeException("Invalid Germplasm Id");
		}

	}

	@Override
	public List<GermplasmDTO> searchGermplasmDTO(
		final GermplasmSearchRequestDto germplasmSearchRequestDTO, final Pageable pageable) {
		try {

			final List<GermplasmDTO> germplasmDTOList = this.germplasmService.searchFilteredGermplasm(germplasmSearchRequestDTO, pageable);
			if (germplasmDTOList != null) {
				this.populateGermplasmPedigree(germplasmDTOList);
			}
			return germplasmDTOList;
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to search germplasm", e);
		}
	}

	@Override
	public long countGermplasmDTOs(final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		try {
			return this.germplasmService.countFilteredGermplasm(germplasmSearchRequestDTO);
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to count germplasm", e);
		}
	}

	@Override
	public long countGermplasmByStudy(final Integer studyDbId) {
		try {
			return this.germplasmService.countGermplasmByStudy(studyDbId);
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to count germplasms", e);
		}
	}

	@Override
	public List<GermplasmDTO> getGermplasmByStudy(final int studyDbId, final Pageable pageable) {
		try {

			this.instanceValidator.validateStudyDbId(studyDbId);

			final List<GermplasmDTO> germplasmDTOList = this.germplasmService.getGermplasmByStudy(studyDbId, pageable);
			if (germplasmDTOList != null) {
				this.populateGermplasmPedigree(germplasmDTOList);
			}
			return germplasmDTOList;
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to search germplasms", e);
		}
	}

	private void populateGermplasmPedigree(final List<GermplasmDTO> germplasmDTOList) {
		final Set<Integer> gids = germplasmDTOList.stream().map(germplasmDTO -> Integer.valueOf(germplasmDTO.getGid()))
			.collect(Collectors.toSet());
		final Map<Integer, String> crossExpansionsMap =
			this.pedigreeService.getCrossExpansions(gids, null, this.crossExpansionProperties);
		for (final GermplasmDTO germplasmDTO : germplasmDTOList) {
			final Integer gid = Integer.valueOf(germplasmDTO.getGid());
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
	public Set<Integer> importGermplasmUpdates(final String programUUID, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		this.germplasmUpdateDtoValidator.validate(programUUID, germplasmUpdateDTOList);

		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
		return this.germplasmService.importGermplasmUpdates(user.getUserid(), germplasmUpdateDTOList);

	}

	@Override
	public List<GermplasmNameTypeDTO> filterGermplasmNameTypes(final Set<String> codes) {

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
	public List<org.generationcp.middleware.api.attribute.AttributeDTO> filterGermplasmAttributes(final Set<String> codes) {

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
	public Map<Integer, GermplasmImportResponseDto> importGermplasm(final String cropName, final String programUUID,
		final GermplasmImportRequestDto germplasmImportRequestDto) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
		this.germplasmImportRequestDtoValidator.validateBeforeSaving(programUUID, germplasmImportRequestDto);
		return this.germplasmService.importGermplasm(user.getUserid(), cropName, germplasmImportRequestDto);
	}

	@Override
	public long countGermplasmMatches(final GermplasmMatchRequestDto germplasmMatchRequestDto) {
		BaseValidator.checkNotNull(germplasmMatchRequestDto, "germplasm.match.request.null");
		if (!germplasmMatchRequestDto.isValid()) {
			this.errors = new MapBindingResult(new HashMap<String, String>(), GermplasmMatchRequestDto.class.getName());
			this.errors.reject("germplasm.match.request.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		return this.germplasmService.countGermplasmMatches(germplasmMatchRequestDto);
	}

	@Override
	public List<GermplasmDto> findGermplasmMatches(final GermplasmMatchRequestDto germplasmMatchRequestDto, final Pageable pageable) {
		BaseValidator.checkNotNull(germplasmMatchRequestDto, "germplasm.match.request.null");
		if (!germplasmMatchRequestDto.isValid()) {
			this.errors = new MapBindingResult(new HashMap<String, String>(), GermplasmMatchRequestDto.class.getName());
			this.errors.reject("germplasm.match.request.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		return this.germplasmService.findGermplasmMatches(germplasmMatchRequestDto, pageable);
	}

	@Override
	public GermplasmDeleteResponse deleteGermplasm(final List<Integer> gids) {

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(this.errors, gids);

		final Set<Integer> invalidGidsForDeletion = this.germplasmDeleteValidator.checkInvalidGidsForDeletion(gids);

		final Set<Integer> validGermplasmForDeletion =
			gids.stream().filter(gid -> !invalidGidsForDeletion.contains(gid)).collect(Collectors.toSet());

		if (!CollectionUtils.isEmpty(validGermplasmForDeletion)) {
			this.germplasmService.deleteGermplasm(new ArrayList<>(validGermplasmForDeletion));
		}

		return new GermplasmDeleteResponse(invalidGidsForDeletion, validGermplasmForDeletion);
	}


	@Override
	public GermplasmImportResponse createGermplasm(final String cropName, final List<GermplasmImportRequest> germplasmImportRequestList) {
		final GermplasmImportResponse response = new GermplasmImportResponse();
		final Integer originalListSize = germplasmImportRequestList.size();
		int noOfCreatedGermplasm = 0;
		// Remove germplasm that fails any validation. They will be excluded from creation
		final BindingResult bindingResult = this.germplasmImportValidator.pruneGermplasmInvalidForImport(germplasmImportRequestList);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}
		if (!CollectionUtils.isEmpty(germplasmImportRequestList)) {
			final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
			final List<GermplasmDTO> germplasmDTOList = germplasmService.createGermplasm(user.getUserid(), cropName, germplasmImportRequestList);
			if (!CollectionUtils.isEmpty(germplasmDTOList)) {
				this.populateGermplasmPedigree(germplasmDTOList);
				noOfCreatedGermplasm = germplasmDTOList.size();
			}
			response.setGermplasmList(germplasmDTOList);
		}
		response.setStatus(noOfCreatedGermplasm + " out of " + originalListSize + " germplasm created successfully.");
		return response;
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

	void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

}
