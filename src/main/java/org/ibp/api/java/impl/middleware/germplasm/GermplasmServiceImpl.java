package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.collections4.CollectionUtils;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.germplasm.GermplasmBasicDetailsDto;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.GermplasmMergeRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmMergeSummaryDto;
import org.generationcp.middleware.domain.germplasm.GermplasmMergedDto;
import org.generationcp.middleware.domain.germplasm.GermplasmProgenyDto;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.domain.germplasm.ProgenitorsDetailsDto;
import org.generationcp.middleware.domain.germplasm.ProgenitorsUpdateRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportResponseDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmMatchRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.germplasm.GermplasmDeleteResponse;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmDeleteValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmMergeRequestDtoValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmUpdateDtoValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmBasicDetailsValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmImportRequestDtoValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.ProgenitorsUpdateRequestDtoValidator;
import org.ibp.api.java.inventory.manager.LotService;
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
import java.util.Objects;
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
	private GermplasmUpdateDtoValidator germplasmUpdateDtoValidator;

	@Autowired
	private GermplasmDeleteValidator germplasmDeleteValidator;

	private BindingResult errors;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private PedigreeDataManager pedigreeDataManager;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	@Autowired
	private GermplasmSearchService germplasmSearchService;

	@Autowired
	private GermplasmNameTypeService germplasmNameTypeService;

	@Autowired
	private GermplasmListService germplasmListService;

	@Autowired
	private LotService lotService;

	@Autowired
	private StudyService studyService;

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmService germplasmService;

	@Autowired
	private GermplasmImportRequestDtoValidator germplasmImportRequestDtoValidator;

	@Autowired
	private GermplasmBasicDetailsValidator germplasmBasicDetailsValidator;

	@Autowired
	private ProgenitorsUpdateRequestDtoValidator progenitorsUpdateRequestDtoValidator;

	@Autowired
	private GermplasmMergeRequestDtoValidator germplasmMergeRequestDtoValidator;

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
		this.addHasProgenyAttribute(responseMap, germplasmSearchRequest);
		this.addUsedInLockedStudyAttribute(responseMap, germplasmSearchRequest);
		this.addUsedInLockedListAttribute(responseMap, germplasmSearchRequest);

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

	private void addHasProgenyAttribute(final Map<Integer, GermplasmSearchResponse> responseMap,
		final GermplasmSearchRequest germplasmSearchRequest) {
		final List<String> addedColumnsPropertyIds = germplasmSearchRequest.getAddedColumnsPropertyIds();
		if (addedColumnsPropertyIds == null || addedColumnsPropertyIds.isEmpty()
			|| !addedColumnsPropertyIds.contains(ColumnLabels.HAS_PROGENY.getName())) {
			return;
		}

		final Set<Integer> gidsOfGermplasmWithDescendants =
			this.germplasmService.getGidsOfGermplasmWithDescendants(new ArrayList<>(responseMap.keySet()));

		responseMap.forEach((gid, response) -> response.setHasProgeny(gidsOfGermplasmWithDescendants.contains(gid)));

	}

	private void addUsedInLockedStudyAttribute(final Map<Integer, GermplasmSearchResponse> responseMap,
		final GermplasmSearchRequest germplasmSearchRequest) {
		final List<String> addedColumnsPropertyIds = germplasmSearchRequest.getAddedColumnsPropertyIds();
		if (addedColumnsPropertyIds == null || addedColumnsPropertyIds.isEmpty()
			|| !addedColumnsPropertyIds.contains(ColumnLabels.USED_IN_LOCKED_STUDY.getName())) {
			return;
		}

		final Set<Integer> germplasmUsedInLockedStudies =
			this.germplasmService.getGermplasmUsedInLockedStudies(new ArrayList<>(responseMap.keySet()));

		responseMap.forEach((gid, response) -> response.setUsedInLockedStudy(germplasmUsedInLockedStudies.contains(gid)));
	}

	private void addUsedInLockedListAttribute(final Map<Integer, GermplasmSearchResponse> responseMap,
		final GermplasmSearchRequest germplasmSearchRequest) {
		final List<String> addedColumnsPropertyIds = germplasmSearchRequest.getAddedColumnsPropertyIds();
		if (addedColumnsPropertyIds == null || addedColumnsPropertyIds.isEmpty()
			|| !addedColumnsPropertyIds.contains(ColumnLabels.USED_IN_LOCKED_LIST.getName())) {
			return;
		}

		final Set<Integer> gidsOfGermplasmInLockedLists =
			this.germplasmService.getGermplasmUsedInLockedList(new ArrayList<>(responseMap.keySet()));

		responseMap.forEach((gid, response) -> response.setUsedInLockedList(gidsOfGermplasmInLockedLists.contains(gid)));
	}

	@Override
	public long countSearchGermplasm(final GermplasmSearchRequest germplasmSearchRequest, final String programUUID) {
		return this.germplasmSearchService.countSearchGermplasm(germplasmSearchRequest, programUUID);
	}

	@Override
	public List<org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO> searchNameTypes(final String query) {
		return this.germplasmNameTypeService.searchNameTypes(query);
	}

	@Override
	public Set<Integer> importGermplasmUpdates(final String programUUID, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		this.germplasmUpdateDtoValidator.validate(programUUID, germplasmUpdateDTOList);

		return this.germplasmService.importGermplasmUpdates(programUUID, germplasmUpdateDTOList);
	}

	@Override
	public List<GermplasmNameTypeDTO> filterGermplasmNameTypes(final Set<String> codes) {
		return this.germplasmNameTypeService.filterGermplasmNameTypes(codes);
	}

	@Override
	public Map<Integer, GermplasmImportResponseDto> importGermplasm(final String cropName, final String programUUID,
		final GermplasmImportRequestDto germplasmImportRequestDto) {
		this.germplasmImportRequestDtoValidator.validateBeforeSaving(programUUID, germplasmImportRequestDto);
		return this.germplasmService.importGermplasm(cropName, programUUID, germplasmImportRequestDto);
	}

	@Override
	public long countGermplasmMatches(final GermplasmMatchRequestDto germplasmMatchRequestDto) {
		BaseValidator.checkNotNull(germplasmMatchRequestDto, "germplasm.match.request.null");
		if (!germplasmMatchRequestDto.isValid()) {
			this.errors = new MapBindingResult(new HashMap<>(), GermplasmMatchRequestDto.class.getName());
			this.errors.reject("germplasm.match.request.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		return this.germplasmService.countGermplasmMatches(germplasmMatchRequestDto);
	}

	@Override
	public List<GermplasmDto> findGermplasmMatches(final GermplasmMatchRequestDto germplasmMatchRequestDto, final Pageable pageable) {
		BaseValidator.checkNotNull(germplasmMatchRequestDto, "germplasm.match.request.null");
		if (!germplasmMatchRequestDto.isValid()) {
			this.errors = new MapBindingResult(new HashMap<>(), GermplasmMatchRequestDto.class.getName());
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
	public GermplasmDto getGermplasmDtoById(final Integer gid) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		final GermplasmDto germplasmDto = this.germplasmService.getGermplasmDtoById(gid);

		if (germplasmDto == null) {
			this.errors.reject("gids.invalid", new String[] {gid.toString()}, "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
		return germplasmDto;
	}

	@Override
	public ProgenitorsDetailsDto getGermplasmProgenitorDetails(final Integer gid) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(this.errors, Collections.singletonList(gid));
		return this.germplasmService.getGermplasmProgenitorDetails(gid);
	}

	@Override
	public boolean updateGermplasmBasicDetails(final Integer gid, final GermplasmBasicDetailsDto germplasmBasicDetailsDto) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(this.errors, Collections.singletonList(gid));
		this.germplasmBasicDetailsValidator.validate(germplasmBasicDetailsDto);
		if (germplasmBasicDetailsDto.allAttributesNull()) {
			return false;
		}
		this.germplasmService.updateGermplasmBasicDetails(gid, germplasmBasicDetailsDto);
		return true;
	}

	@Override
	public boolean updateGermplasmPedigree(final String programUUID, final Integer gid,
		final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(this.errors, Collections.singletonList(gid));
		if (Objects.nonNull(progenitorsUpdateRequestDto) && progenitorsUpdateRequestDto.allAttributesNull()) {
			return false;
		}
		this.progenitorsUpdateRequestDtoValidator.validate(gid, progenitorsUpdateRequestDto);
		this.germplasmService.updateGermplasmPedigree(gid, progenitorsUpdateRequestDto);
		return true;
	}

	@Override
	public void mergeGermplasm(final GermplasmMergeRequestDto germplasmMergeRequestDto) {
		this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
		this.germplasmService.mergeGermplasm(germplasmMergeRequestDto,
			this.pedigreeService.getCrossExpansion(germplasmMergeRequestDto.getTargetGermplasmId(), this.crossExpansionProperties));
	}

	@Override
	public List<GermplasmMergedDto> getGermplasmMerged(final Integer gid) {
		this.germplasmValidator.validateGermplasmId(this.errors, gid);
		return this.germplasmService.getGermplasmMerged(gid);
	}

	@Override
	public List<GermplasmProgenyDto> getGermplasmProgenies(final Integer gid) {
		this.germplasmValidator.validateGermplasmId(this.errors, gid);
		return this.germplasmService.getGermplasmProgenies(gid);
	}

	@Override
	public GermplasmMergeSummaryDto getGermplasmMergeSummary(final GermplasmMergeRequestDto germplasmMergeRequestDto) {
		this.germplasmMergeRequestDtoValidator.validate(germplasmMergeRequestDto);
		// Remove non-selected germplasm for omission
		germplasmMergeRequestDto.getNonSelectedGermplasm().removeIf(GermplasmMergeRequestDto.NonSelectedGermplasm::isOmit);

		final GermplasmMergeSummaryDto germplasmMergeSummaryDto = new GermplasmMergeSummaryDto();
		germplasmMergeSummaryDto.setCountGermplasmToDelete(germplasmMergeRequestDto.getNonSelectedGermplasm().size());

		final List<Integer> nonSelectedGids = germplasmMergeRequestDto.getNonSelectedGermplasm().stream().map(
			GermplasmMergeRequestDto.NonSelectedGermplasm::getGermplasmId).collect(Collectors.toList());
		germplasmMergeSummaryDto.setCountListsToUpdate(this.germplasmListService.countGermplasmLists(nonSelectedGids));
		germplasmMergeSummaryDto.setCountStudiesToUpdate(this.studyService.countStudiesByGids(nonSelectedGids));
		germplasmMergeSummaryDto.setCountPlotsToUpdate(this.studyService.countPlotsByGids(nonSelectedGids));

		final List<Integer> migrateLotsGids =
			germplasmMergeRequestDto.getNonSelectedGermplasm().stream().filter(GermplasmMergeRequestDto.NonSelectedGermplasm::isMigrateLots)
				.map(GermplasmMergeRequestDto.NonSelectedGermplasm::getGermplasmId).collect(
				Collectors.toList());
		if (!CollectionUtils.isEmpty(migrateLotsGids)) {
			final LotsSearchDto migrateLotSearch = new LotsSearchDto();
			migrateLotSearch.setGids(migrateLotsGids);
			germplasmMergeSummaryDto.setCountLotsToMigrate(this.lotService.countSearchLots(migrateLotSearch));
		}

		final List<Integer> closeLotsGids =
			germplasmMergeRequestDto.getNonSelectedGermplasm().stream().filter(g -> !g.isMigrateLots())
				.map(GermplasmMergeRequestDto.NonSelectedGermplasm::getGermplasmId).collect(
				Collectors.toList());
		if (!CollectionUtils.isEmpty(closeLotsGids)) {
			final LotsSearchDto closeLotSearch = new LotsSearchDto();
			closeLotSearch.setGids(closeLotsGids);
			germplasmMergeSummaryDto.setCountLotsToClose(this.lotService.countSearchLots(closeLotSearch));
		}
		return germplasmMergeSummaryDto;
	}


}
