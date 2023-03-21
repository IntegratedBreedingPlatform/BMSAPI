package org.ibp.api.java.impl.middleware.study;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyEntryGeneratorRequestDto;
import org.generationcp.middleware.domain.study.StudyEntryPropertyBatchUpdateRequest;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.StockPropertyData;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsIn;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.study.StudyEntryDetailsImportRequest;
import org.ibp.api.domain.study.StudyEntryDetailsValueMap;
import org.ibp.api.java.entrytype.EntryTypeService;
import org.ibp.api.java.impl.middleware.common.validator.EntryTypeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.common.SearchRequestDtoResolver;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class StudyEntryServiceImplTest {

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private StudyEntryValidator studyEntryValidator;

	@Mock
	private EntryTypeValidator entryTypeValidator;

	@Mock
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Mock
	private GermplasmValidator germplasmValidator;

	@Mock
	private SearchRequestDtoResolver searchRequestDtoResolver;

	@Mock
	private GermplasmListValidator germplasmListValidator;

	@Mock
	private TermValidator termValidator;

	@Mock
	private StudyEntryService middlewareStudyEntryService;

	@Mock
	private DatasetService mwDatasetService;

	@Mock
	private org.ibp.api.java.dataset.DatasetService datasetService;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private EntryTypeService entryTypeService;

	@InjectMocks
	private final StudyEntryServiceImpl studyEntryService = new StudyEntryServiceImpl();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	private final Random random = new Random();

	@Test
	public void testReplaceStudyEntry() {
		final Integer studyId = this.random.nextInt();
		final Integer entryId = this.random.nextInt();
		final Integer newGid = this.random.nextInt();
		final StudyEntryDto dto = new StudyEntryDto();
		dto.setGid(newGid);
		this.studyEntryService.replaceStudyEntry(studyId, entryId, dto);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.studyEntryValidator).validate(studyId, entryId, newGid);
		Mockito.verify(this.middlewareStudyEntryService).replaceStudyEntry(studyId, entryId, newGid);
	}

	@Test
	public void testCreateStudyEntries() {
		final Integer studyId = this.random.nextInt();
		final Integer gid = this.random.nextInt();
		final List<Integer> gids = Collections.singletonList(gid);
		final StudyEntryGeneratorRequestDto studyEntryGeneratorRequestDto = new StudyEntryGeneratorRequestDto();
		studyEntryGeneratorRequestDto.setEntryTypeId(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
		final SearchCompositeDto searchCompositeDto = new SearchCompositeDto();
		searchCompositeDto.setItemIds(new HashSet(gids));
		studyEntryGeneratorRequestDto.setSearchComposite(searchCompositeDto);

		Mockito.when(this.searchRequestDtoResolver.resolveGidSearchDto(searchCompositeDto)).thenReturn(gids);

		Mockito.doNothing().when(this.middlewareStudyEntryService)
			.saveStudyEntries(studyId, gids, SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());

		this.studyEntryService.createStudyEntries(studyId, studyEntryGeneratorRequestDto);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.entryTypeValidator).validateEntryType(studyEntryGeneratorRequestDto.getEntryTypeId());
		Mockito.verify(this.searchCompositeDtoValidator)
			.validateSearchCompositeDto(ArgumentMatchers.eq(searchCompositeDto), ArgumentMatchers.any(BindingResult.class));
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(gids));
		Mockito.verify(this.middlewareStudyEntryService)
			.saveStudyEntries(studyId, gids, SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());

		Mockito.verifyNoMoreInteractions(this.studyValidator);
		Mockito.verifyNoMoreInteractions(this.entryTypeValidator);
		Mockito.verifyNoMoreInteractions(this.searchCompositeDtoValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmValidator);
		Mockito.verifyNoMoreInteractions(this.middlewareStudyEntryService);
	}

	@Test
	public void testCreateStudyEntriesList() {

		final Integer studyId = this.random.nextInt();
		final Integer germplasmListId = this.random.nextInt();

		Mockito.doNothing().when(this.middlewareStudyEntryService).saveStudyEntries(studyId, germplasmListId);

		this.studyEntryService.createStudyEntries(studyId, germplasmListId);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.germplasmListValidator).validateGermplasmList(germplasmListId);
		Mockito.verify(this.studyEntryValidator).validateStudyAlreadyHasStudyEntries(studyId);
		Mockito.verify(this.middlewareStudyEntryService).saveStudyEntries(studyId, germplasmListId);

		Mockito.verifyNoMoreInteractions(this.studyValidator);
		Mockito.verifyNoMoreInteractions(this.germplasmListValidator);
		Mockito.verifyNoMoreInteractions(this.studyEntryValidator);
		Mockito.verifyNoMoreInteractions(this.middlewareStudyEntryService);
	}

	@Test
	public void testGetStudyEntriesMetadata() {
		final int studyId = this.random.nextInt();
		final String programUUID = UUID.randomUUID().toString();
		final Long testEntriesCount = Long.valueOf(5);
		final Long checkEntriesCount = Long.valueOf(3);
		final Long nonTestEntriesCount = Long.valueOf(2);
		final Long nonReplicatedEntriesCount = Long.valueOf(3);

		Mockito.when(this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId,
				Collections.singletonList(Integer.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()))))
			.thenReturn(testEntriesCount);
		Mockito.when(this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId,
				Collections.singletonList(Integer.valueOf(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()))))
			.thenReturn(checkEntriesCount);
		Mockito.when(this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId,
				Collections.singletonList(Integer.valueOf(SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeCategoricalId()))))
			.thenReturn(nonReplicatedEntriesCount);

		final List<Enumeration> enumerations = new ArrayList<>();
		enumerations.add(new Enumeration(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId(),
			SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName(), SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue(), 1));
		enumerations.add(new Enumeration(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId(),
			SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeName(), SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeValue(), 2));
		enumerations.add(new Enumeration(SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeCategoricalId(),
			SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeName(), SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeValue(), 3));
		enumerations.add(new Enumeration(SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeCategoricalId(),
			SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeName(), SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeValue(),
			4));

		Mockito.when(this.entryTypeService.getEntryTypes(programUUID)).thenReturn(enumerations);

		final List<Integer> checkEntryTypeIds = enumerations.stream()
			.filter(entryType -> entryType.getId() != SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())
			.map(entryType -> entryType.getId()).collect(Collectors.toList());

		final List<Integer> nonReplicatedEntryTypeIds = enumerations.stream()
			.filter(entryType -> entryType.getId() == SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeCategoricalId())
			.map(entryType -> entryType.getId()).collect(Collectors.toList());

		Mockito.when(this.middlewareStudyEntryService
			.countStudyGermplasmByEntryTypeIds(studyId, checkEntryTypeIds)).thenReturn(nonTestEntriesCount);
		Mockito.when(this.middlewareStudyEntryService.hasUnassignedEntries(studyId)).thenReturn(false);

		final StudyEntryMetadata metadata = this.studyEntryService.getStudyEntriesMetadata(studyId, programUUID);
		Assert.assertEquals(testEntriesCount, metadata.getTestEntriesCount());
		Assert.assertEquals(checkEntriesCount, metadata.getCheckEntriesCount());
		Assert.assertEquals(nonTestEntriesCount, metadata.getNonTestEntriesCount());
		Assert.assertEquals(nonReplicatedEntriesCount, metadata.getNonReplicatedEntriesCount());
		Assert.assertFalse(metadata.getHasUnassignedEntries());
	}

	@Test
	public void testDeleteStudyGermplasm() {
		final Integer studyId = this.random.nextInt();
		this.studyEntryService.deleteStudyEntries(studyId);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.middlewareStudyEntryService).deleteStudyEntries(studyId);
	}

	@Test
	public void testUpdateStudyEntriesProperty() {

		final Integer studyId = this.random.nextInt();
		final Integer entryId = this.random.nextInt();
		final Integer variableId = this.random.nextInt();
		final SearchCompositeDto searchCompositeDto = new SearchCompositeDto();
		searchCompositeDto.setItemIds(new HashSet(Collections.singletonList(entryId)));
		final StudyEntryPropertyBatchUpdateRequest requestDto = new StudyEntryPropertyBatchUpdateRequest(searchCompositeDto,
			variableId, String.valueOf(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()));
		this.studyEntryService.updateStudyEntriesProperty(studyId, requestDto);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.studyEntryValidator).validateStudyContainsEntries(studyId, new ArrayList<>(searchCompositeDto.getItemIds()));
		Mockito.verify(this.studyEntryValidator).validateStudyContainsEntries(studyId, new ArrayList<>(searchCompositeDto.getItemIds()));
		Mockito.verify(this.termValidator).validate(variableId);
		Mockito.verify(this.middlewareStudyEntryService).updateStudyEntriesProperty(requestDto);
	}

	@Test
	public void testImportUpdates() {

		final Integer studyId = this.random.nextInt();
		final Integer datasetId = this.random.nextInt();
		final String entryId = this.random.toString();
		final Integer variableId = this.random.nextInt();

		final StudyEntryDetailsValueMap valueMap = new StudyEntryDetailsValueMap();
		valueMap.setEntryNumber(entryId);
		valueMap.setData(Arrays.asList(new StockPropertyData(1, variableId, "sample", null)));

		final List<DatasetVariable> datasetVariables = Arrays.asList(new DatasetVariable());

		final StudyEntryDetailsImportRequest studyEntryDetailsImportRequest = new StudyEntryDetailsImportRequest(
			Arrays.asList(valueMap), datasetVariables
		);

		org.ibp.api.rest.dataset.DatasetDTO dataset = new org.ibp.api.rest.dataset.DatasetDTO();
		dataset.setDatasetId(datasetId);
		Mockito.when(this.datasetService.getDatasets(
			studyId, Collections.singleton(DatasetTypeEnum.PLOT_DATA.getId())))
			.thenReturn(Arrays.asList(dataset));

		this.studyEntryService.importUpdates(studyId, studyEntryDetailsImportRequest);

		Mockito.verify(this.datasetService).addDatasetVariables(studyId, datasetId, datasetVariables, VariableType.ENTRY_DETAIL);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.studyEntryValidator).validateStudyContainsEntryNumbers(studyId, Collections.singleton(entryId));
		Mockito.verify(this.middlewareStudyEntryService).getStudyEntries(ArgumentMatchers.eq(studyId),
			ArgumentMatchers.any(StudyEntrySearchDto.Filter.class),
			ArgumentMatchers.eq(new PageRequest(0, Integer.MAX_VALUE)));
	}

	@Test
	public void testGetEntryDescriptorColumns() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetId(datasetId);
		datasetDTO.setDatasetTypeId(DatasetTypeEnum.PLOT_DATA.getId());
		final List<DatasetDTO> datasetDTOS = Collections.singletonList(datasetDTO);
		Mockito.when(this.mwDatasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))))
			.thenReturn(datasetDTOS);

		final MeasurementVariable entryCodeVariable = new MeasurementVariable(TermId.ENTRY_CODE.getId());
		final MeasurementVariable entryNoVariable = new MeasurementVariable(TermId.ENTRY_NO.getId());
		final MeasurementVariable designationVariable = new MeasurementVariable(TermId.DESIG.getId());
		final MeasurementVariable crossVariable = new MeasurementVariable(TermId.CROSS.getId());
		final MeasurementVariable gidVariable = new MeasurementVariable(TermId.GID.getId());

		final List<MeasurementVariable> measurementVariables = Lists
			.newArrayList(entryCodeVariable, entryNoVariable, designationVariable,
				crossVariable, gidVariable);

		Mockito.when(
			this.mwDatasetService.getObservationSetVariables(datasetId, Lists.newArrayList(VariableType.GERMPLASM_ATTRIBUTE.getId(),
				VariableType.GERMPLASM_PASSPORT.getId(),
				VariableType.GERMPLASM_DESCRIPTOR.getId(),
				VariableType.ENTRY_DETAIL.getId()))).thenReturn(measurementVariables);

		final List<MeasurementVariable> results = this.studyEntryService.getEntryTableHeader(studyId);

		MatcherAssert.assertThat(results, IsCollectionWithSize.hasSize(9));
		MatcherAssert.assertThat(entryCodeVariable, IsIn.in(results));
		MatcherAssert.assertThat(entryNoVariable, IsIn.in(results));
		MatcherAssert.assertThat(designationVariable, IsIn.in(results));
		MatcherAssert.assertThat(crossVariable, IsIn.in(results));
		MatcherAssert.assertThat(gidVariable, IsIn.in(results));
		MatcherAssert.assertThat(new MeasurementVariable(TermId.GID_UNIT.getId()), IsIn.in(results));
		MatcherAssert.assertThat(new MeasurementVariable(TermId.GID_AVAILABLE_BALANCE.getId()), IsIn.in(results));
		MatcherAssert.assertThat(new MeasurementVariable(TermId.GID_ACTIVE_LOTS_COUNT.getId()), IsIn.in(results));
	}

}
