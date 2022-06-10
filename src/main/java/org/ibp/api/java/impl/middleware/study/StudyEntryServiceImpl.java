package org.ibp.api.java.impl.middleware.study;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyEntryGeneratorRequestDto;
import org.generationcp.middleware.domain.study.StudyEntryPropertyBatchUpdateRequest;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyEntryColumnDTO;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.impl.study.StudyEntryDescriptorColumns;
import org.ibp.api.java.entrytype.EntryTypeService;
import org.ibp.api.java.impl.middleware.common.validator.EntryTypeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.common.SearchRequestDtoResolver;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyEntryServiceImpl implements StudyEntryService {

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private StudyEntryValidator studyEntryValidator;

	@Autowired
	private GermplasmListValidator germplasmListValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private EntryTypeValidator entryTypeValidator;

	@Autowired
	private SearchRequestDtoResolver searchRequestDtoResolver;

	@Autowired
	private TermValidator termValidator;

	@Autowired
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Resource
	private org.generationcp.middleware.service.api.study.StudyEntryService middlewareStudyEntryService;

	@Resource
	private DatasetService datasetService;

	@Resource
	private EntryTypeService entryTypeService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Override
	public void replaceStudyEntry(final Integer studyId, final Integer entryId, final StudyEntryDto studyEntryDto) {
		final Integer gid = studyEntryDto.getGid();
		this.studyValidator.validate(studyId, true);
		this.studyEntryValidator.validate(studyId, entryId, gid);

		this.middlewareStudyEntryService.replaceStudyEntry(studyId, entryId, gid);
	}

	@Override
	public void createStudyEntries(final Integer studyId, final StudyEntryGeneratorRequestDto studyEntryGeneratorRequestDto) {
		this.studyValidator.validate(studyId, true);

		//Validate EntryType
		this.entryTypeValidator.validateEntryType(studyEntryGeneratorRequestDto.getEntryTypeId());

		final SearchCompositeDto<Integer, Integer> searchComposite = studyEntryGeneratorRequestDto.getSearchComposite();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), StudyEntryGeneratorRequestDto.class.getName());
		this.searchCompositeDtoValidator.validateSearchCompositeDto(searchComposite, errors);
		final List<Integer> gids = this.searchRequestDtoResolver.resolveGidSearchDto(searchComposite);
		this.germplasmValidator.validateGids(errors, gids);

		this.middlewareStudyEntryService.saveStudyEntries(studyId, gids, studyEntryGeneratorRequestDto.getEntryTypeId());
	}

	@Override
	public void createStudyEntries(final Integer studyId, final Integer listId) {
		this.studyValidator.validate(studyId, true);

		this.germplasmListValidator.validateGermplasmList(listId);
		this.studyEntryValidator.validateStudyAlreadyHasStudyEntries(studyId);

		this.middlewareStudyEntryService.saveStudyEntries(studyId, listId);
	}

	@Override
	public void deleteStudyEntries(final Integer studyId) {
		this.studyValidator.validate(studyId, true);
		this.studyValidator.validateStudyShouldNotHaveObservation(studyId);
		this.middlewareStudyEntryService.deleteStudyEntries(studyId);
	}

	@Override
	public void updateStudyEntriesProperty(final Integer studyId,
		final StudyEntryPropertyBatchUpdateRequest batchUpdateRequest) {
		this.studyValidator.validate(studyId, true);
		this.studyValidator.validateStudyShouldNotHaveObservation(studyId);
		this.studyEntryValidator.validateStudyContainsEntries(studyId, new ArrayList<>(batchUpdateRequest.getSearchComposite().getItemIds()));
		this.termValidator.validate(batchUpdateRequest.getVariableId());
		this.middlewareStudyEntryService.updateStudyEntriesProperty(batchUpdateRequest);
	}

	@Override
	public long countFilteredStudyEntries(final Integer studyId, final StudyEntrySearchDto.Filter filter) {
		return this.middlewareStudyEntryService.countFilteredStudyEntries(studyId, filter);
	}

	@Override
	public List<StudyEntryDto> getStudyEntries(final Integer studyId, final StudyEntrySearchDto.Filter filter, final Pageable pageable) {
		this.studyValidator.validate(studyId, false);

		Pageable convertedPageable = null;
		if (pageable != null && pageable.getSort() != null) {
			final Iterator<Sort.Order> iterator = pageable.getSort().iterator();
			if (iterator.hasNext()) {
				// Convert the sort property name from termid to actual term name.
				final Sort.Order sort = iterator.next();
				final Term term = this.ontologyDataManager.getTermById(Integer.valueOf(sort.getProperty()));
				final String sortProperty = Objects.isNull(term) ? sort.getProperty() : term.getName();
				pageable.getSort().and(new Sort(sort.getDirection(), sortProperty));
				convertedPageable =
					new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort.getDirection(),
						sortProperty);
			}
		}

		return this.middlewareStudyEntryService.getStudyEntries(studyId, filter, convertedPageable);
	}

	@Override
	public long countAllStudyEntries(final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		return this.middlewareStudyEntryService.countStudyEntries(studyId);
	}

	@Override
	public List<MeasurementVariable> getEntryTableHeader(final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		final Integer plotDatasetId =
			this.datasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))).get(0).getDatasetId();

		final List<Integer> termsToRemove = Lists
			.newArrayList(TermId.OBS_UNIT_ID.getId());

		final List<MeasurementVariable> columns =
			this.datasetService.getObservationSetVariables(plotDatasetId,
				Lists.newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.ENTRY_DETAIL.getId()));

		//Remove OBS_UNIT_ID column if present
		columns.removeIf(entry -> termsToRemove.contains(entry.getTermId()));

		final List<MeasurementVariable> descriptors = new ArrayList<>();
		final Map<Integer, MeasurementVariable> entryDetails = new HashMap<>();
		columns.stream().forEach(variable -> {
			if (variable.getVariableType() == VariableType.ENTRY_DETAIL) {
				entryDetails.put(variable.getTermId(), variable);
			} else {
				descriptors.add(variable);
			}
		});

		descriptors.sort(
			Comparator.nullsLast(Comparator.comparing(descriptor -> {
				final StudyEntryDescriptorColumns column = StudyEntryDescriptorColumns.getByTermId(descriptor.getTermId());
				return column == null ? Integer.MAX_VALUE : column.getRank();
			})));

		final List<MeasurementVariable> orderedColumns = new ArrayList<>();
		orderedColumns.add(entryDetails.remove(TermId.ENTRY_NO.getId()));
		orderedColumns.add(entryDetails.remove(TermId.ENTRY_TYPE.getId()));

		orderedColumns.addAll(descriptors);

		//Add Inventory related columns
		orderedColumns.add(this.buildVirtualColumn("LOTS", TermId.GID_ACTIVE_LOTS_COUNT));
		orderedColumns.add(this.buildVirtualColumn("AVAILABLE", TermId.GID_AVAILABLE_BALANCE));
		orderedColumns.add(this.buildVirtualColumn("UNIT", TermId.GID_UNIT));

		orderedColumns.addAll(entryDetails.values());

		return orderedColumns;
	}

	@Override
	public long countAllStudyTestEntries(final Integer studyId) {
		return this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId,
			Collections.singletonList(Integer.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())));
	}

	@Override
	public long countAllCheckTestEntries(final Integer studyId, final String programUuid, final Boolean checkOnly) {
		if(checkOnly) {
			return this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId,
				Collections.singletonList(Integer.valueOf(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId())));
		} else {
			final List<Enumeration> entryTypes = this.entryTypeService.getEntryTypes(programUuid);
			final List<Integer> checkEntryTypeIds = entryTypes.stream()
				.filter(entryType -> entryType.getId() != SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())
				.map(entryType -> entryType.getId()).collect(Collectors.toList());
			return this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId, checkEntryTypeIds);
		}
	}

  @Override public long countAllNonReplicatedTestEntries(final Integer studyId) {
	return this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId,
			Collections.singletonList(Integer.valueOf(SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeCategoricalId())));
  }

  @Override
	public StudyEntryMetadata getStudyEntriesMetadata(final Integer studyId, final String programUuid) {
		this.studyValidator.validate(studyId, false);
		final StudyEntryMetadata studyEntryMetadata = new StudyEntryMetadata();
		studyEntryMetadata.setTestEntriesCount(this.countAllStudyTestEntries(studyId));
		studyEntryMetadata.setCheckEntriesCount(this.countAllCheckTestEntries(studyId, programUuid, true));
		studyEntryMetadata.setNonTestEntriesCount(this.countAllCheckTestEntries(studyId, programUuid, false));
		studyEntryMetadata.setHasUnassignedEntries(this.middlewareStudyEntryService.hasUnassignedEntries(studyId));
		studyEntryMetadata.setNonReplicatedEntriesCount(this.countAllNonReplicatedTestEntries(studyId));
		return studyEntryMetadata;
	}

	@Override
	public void fillWithCrossExpansion(final Integer studyId, final Integer level) {
		this.studyValidator.validate(studyId, true);

		this.middlewareStudyEntryService.fillWithCrossExpansion(studyId, level);
	}

	@Override
	public Integer getCrossExpansionLevel(final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		return this.middlewareStudyEntryService.getCrossGenerationLevel(studyId);
	}

	@Override
	public List<StudyEntryColumnDTO> getStudyEntryColumns(final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		return this.middlewareStudyEntryService.getStudyEntryColumns(studyId);
	}

	private MeasurementVariable buildVirtualColumn(final String name, final TermId termId) {
		final MeasurementVariable sampleColumn = new MeasurementVariable();
		sampleColumn.setName(name);
		sampleColumn.setAlias(name);
		sampleColumn.setTermId(termId.getId());
		sampleColumn.setFactor(true);
		return sampleColumn;
	}

	public void setDatasetService(final DatasetService datasetService) {
		this.datasetService = datasetService;
	}

}
