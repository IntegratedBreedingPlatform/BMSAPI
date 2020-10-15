package org.ibp.api.java.impl.middleware.study;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.java.entrytype.EntryTypeService;
import org.ibp.api.java.germplasm.GermplamListService;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyEntryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyEntryServiceImpl implements StudyEntryService {

	@Resource
	private StudyValidator studyValidator;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private StudyEntryValidator studyEntryValidator;

	@Autowired
	private GermplasmListValidator germplasmListValidator;

	@Autowired
	private GermplamListService germplasmListService;

	@Autowired
	private TermValidator termValidator;

	@Resource
	private org.generationcp.middleware.service.api.study.StudyEntryService middlewareStudyEntryService;

	@Resource
	private DatasetService datasetService;

	@Resource
	private EntryTypeService entryTypeService;

	@Override
	public StudyEntryDto replaceStudyEntry(final Integer studyId, final Integer entryId,
		final StudyEntryDto studyEntryDto) {
		final Integer gid = studyEntryDto.getGid();
		this.studyValidator.validate(studyId, true);
		this.studyEntryValidator.validate(studyId, entryId, gid);

		return this.middlewareStudyEntryService
			.replaceStudyEntry(studyId, entryId, gid, this.pedigreeService.getCrossExpansion(gid, this.crossExpansionProperties));
	}

	@Override
	public List<StudyEntryDto> createStudyEntries(final Integer studyId, final Integer germplasmListId) {
		final GermplasmList germplasmList = this.germplasmListService.getGermplasmList(germplasmListId);

		this.germplasmListValidator.validateGermplasmList(germplasmListId);
		this.studyEntryValidator.validateStudyAlreadyHasStudyEntries(studyId);
		this.studyValidator.validate(studyId, true);

		final ModelMapper mapper = StudyEntryMapper.getInstance();
		final List<StudyEntryDto> studyEntryDtoList =
			germplasmList.getListData().stream().map(l -> mapper.map(l, StudyEntryDto.class)).collect(Collectors.toList());

		return this.middlewareStudyEntryService.saveStudyEntries(studyId, studyEntryDtoList);
	}

	@Override
	public void deleteStudyEntries(final Integer studyId) {
		this.studyValidator.validate(studyId, true);
		this.studyValidator.validateStudyShouldNotHaveObservation(studyId);
		this.middlewareStudyEntryService.deleteStudyEntries(studyId);
	}

	@Override
	public void updateStudyEntryProperty(final Integer studyId, final Integer entryId,
		final StudyEntryPropertyData studyEntryPropertyData) {
		this.studyValidator.validate(studyId, true);
		this.studyValidator.validateStudyContainsEntry(studyId, entryId);
		this.termValidator.validate(studyEntryPropertyData.getVariableId());
		this.studyEntryValidator.validateStudyEntryProperty(studyEntryPropertyData.getStudyEntryPropertyId());
		this.middlewareStudyEntryService.updateStudyEntryProperty(studyId, studyEntryPropertyData);
	}

	@Override
	public List<StudyEntryDto> getStudyEntries(final Integer studyId, final StudyEntrySearchDto.Filter filter, final Pageable pageable) {
		this.studyValidator.validate(studyId, false);
		return this.middlewareStudyEntryService.getStudyEntries(studyId, filter, pageable);
	}

	@Override
	public long countAllStudyEntries(final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		return this.middlewareStudyEntryService.countStudyEntries(studyId);
	}

	@Override
	public List<MeasurementVariable> getEntryDescriptorColumns(final Integer studyId) {
		this.studyValidator.validate(studyId, false);
		final Integer plotDatasetId =
			datasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))).get(0).getDatasetId();

		final List<Integer> termsToRemove = Lists
			.newArrayList(TermId.OBS_UNIT_ID.getId(), TermId.STOCKID.getId());

		final List<MeasurementVariable> entryDescriptors =
			this.datasetService.getObservationSetVariables(plotDatasetId, Lists
				.newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId()));

		//Remove OBS_UNIT_ID column and STOCKID if present
		entryDescriptors.removeIf(entry -> termsToRemove.contains(entry.getTermId()));

		//Add Inventory related columns
		entryDescriptors.add(this.buildVirtualColumn("LOTS", TermId.GID_ACTIVE_LOTS_COUNT));
		entryDescriptors.add(this.buildVirtualColumn("AVAILABLE", TermId.GID_AVAILABLE_BALANCE));
		entryDescriptors.add(this.buildVirtualColumn("UNIT", TermId.GID_UNIT));

		return entryDescriptors;
	}

	@Override
	public long countAllStudyTestEntries(final Integer studyId) {
		return this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId,
			Collections.singletonList(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())));
	}

	@Override
	public long countAllCheckTestEntries(final Integer studyId, final String programUuid, final Boolean checkOnly) {
		final List<Enumeration> entryTypes = this.entryTypeService.getEntryTypes(programUuid);
		if(checkOnly) {
			return this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId,
				Collections.singletonList(String.valueOf(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId())));
		} else {
			final List<String> checkEntryTypeIds = entryTypes.stream()
				.filter(enttryType -> enttryType.getId() != SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())
				.map(entryType -> String.valueOf(entryType.getId())).collect(Collectors.toList());
			return this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId, checkEntryTypeIds);
		}
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
