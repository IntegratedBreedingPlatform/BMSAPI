package org.ibp.api.java.impl.middleware.study;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyEntryGeneratorRequestDto;
import org.generationcp.middleware.domain.study.StudyEntryPropertyBatchUpdateRequest;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsIn;
import org.ibp.api.java.entrytype.EntryTypeService;
import org.ibp.api.java.germplasm.GermplasmListService;
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
import org.springframework.validation.BindingResult;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class StudyEntryServiceImplTest {

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

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
	private GermplasmListService germplasmListService;

	@Mock
	private TermValidator termValidator;

	@Mock
	private StudyEntryService middlewareStudyEntryService;

	@Mock
	private DatasetService datasetService;

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
		final String crossExpansion = RandomStringUtils.randomAlphabetic(20);
		Mockito.doReturn(crossExpansion).when(this.pedigreeService).getCrossExpansion(newGid, this.crossExpansionProperties);
		final StudyEntryDto dto = new StudyEntryDto();
		dto.setGid(newGid);
		this.studyEntryService.replaceStudyEntry(studyId, entryId, dto);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.studyEntryValidator).validate(studyId, entryId, newGid);
		Mockito.verify(this.middlewareStudyEntryService).replaceStudyEntry(studyId, entryId, newGid, crossExpansion);
	}

	@Test
	public void testCreateStudyEntries() {
		final Integer studyId = this.random.nextInt();
		final Integer gid = this.random.nextInt();
		final List<Integer> gids = Collections.singletonList(gid);
		final StudyEntryGeneratorRequestDto studyEntryGeneratorRequestDto = new StudyEntryGeneratorRequestDto();
		studyEntryGeneratorRequestDto.setEntryTypeId(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
		final SearchCompositeDto searchCompositeDto = new SearchCompositeDto();
		searchCompositeDto.setItemIds(gids);
		studyEntryGeneratorRequestDto.setSearchComposite(searchCompositeDto);

		Mockito.when(this.searchRequestDtoResolver.resolveGidSearchDto(searchCompositeDto)).thenReturn(gids);
		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(1);
		Mockito.when(this.germplasmDataManager.getGermplasms(gids)).thenReturn(Collections.singletonList(germplasm));
		final Map<Integer, String> gidDesignationMap = new HashMap<>();
		gidDesignationMap.put(gid, "Designation");
		Mockito.when(this.germplasmDataManager.getPreferredNamesByGids(gids)).thenReturn(gidDesignationMap);

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetId(datasetId);
		datasetDTO.setDatasetTypeId(DatasetTypeEnum.PLOT_DATA.getId());
		final List<DatasetDTO> datasetDTOS = Collections.singletonList(datasetDTO);
		Mockito.when(this.datasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))))
			.thenReturn(datasetDTOS);

		final Integer startingEntryNumber = 5;
		Mockito.when(this.middlewareStudyEntryService.getNextEntryNumber(studyId)).thenReturn(startingEntryNumber);

		this.studyEntryService.createStudyEntries(studyId, studyEntryGeneratorRequestDto);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.entryTypeValidator).validateEntryType(studyEntryGeneratorRequestDto.getEntryTypeId());
		Mockito.verify(this.searchCompositeDtoValidator)
			.validateSearchCompositeDto(ArgumentMatchers.eq(searchCompositeDto), ArgumentMatchers.any(BindingResult.class));
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(gids));
	}

	@Test
	public void testCreateStudyEntriesList() {

		final GermplasmList germplasmList = new GermplasmList();
		final List<GermplasmListData> listData = new ArrayList<>();
		germplasmList.setListData(listData);

		final Integer studyId = this.random.nextInt();
		final Integer germplasmListId = this.random.nextInt();

		Mockito.when(this.germplasmListService.getGermplasmList(germplasmListId)).thenReturn(germplasmList);

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetId(datasetId);
		datasetDTO.setDatasetTypeId(DatasetTypeEnum.PLOT_DATA.getId());
		final List<DatasetDTO> datasetDTOS = Collections.singletonList(datasetDTO);
		Mockito.when(this.datasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))))
			.thenReturn(datasetDTOS);

		this.studyEntryService.createStudyEntries(studyId, germplasmListId);

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(germplasmListId);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.studyEntryValidator).validateStudyAlreadyHasStudyEntries(studyId);
		Mockito.verify(this.middlewareStudyEntryService).saveStudyEntries(ArgumentMatchers.eq(studyId), ArgumentMatchers.anyList());

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
			Collections.singletonList(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()))))
			.thenReturn(testEntriesCount);
		Mockito.when(this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId,
			Collections.singletonList(String.valueOf(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()))))
			.thenReturn(checkEntriesCount);
		Mockito.when(this.middlewareStudyEntryService.countStudyGermplasmByEntryTypeIds(studyId,
				Collections.singletonList(String.valueOf(SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeCategoricalId()))))
				.thenReturn(nonReplicatedEntriesCount);

		final List<Enumeration> enumerations = new ArrayList<>();
		enumerations.add(new Enumeration(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId(),
			SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName(),SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue(), 1));
		enumerations.add(new Enumeration(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId(),
			SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeName(),SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeValue(), 2));
		enumerations.add(new Enumeration(SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeCategoricalId(),
			SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeName(),SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeValue(), 3));
		enumerations.add(new Enumeration(SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeCategoricalId(),
				SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeName(),SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeValue(), 4));

		Mockito.when(this.entryTypeService.getEntryTypes(programUUID)).thenReturn(enumerations);

		final List<String> checkEntryTypeIds = enumerations.stream()
			.filter(entryType -> entryType.getId() != SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())
			.map(entryType -> String.valueOf(entryType.getId())).collect(Collectors.toList());

		final List<String> nonReplicatedEntryTypeIds = enumerations.stream()
				.filter(entryType -> entryType.getId() == SystemDefinedEntryType.NON_REPLICATED_ENTRY.getEntryTypeCategoricalId())
				.map(entryType -> String.valueOf(entryType.getId())).collect(Collectors.toList());


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
		searchCompositeDto.setItemIds(Collections.singletonList(entryId));
		final StudyEntryPropertyBatchUpdateRequest requestDto = new StudyEntryPropertyBatchUpdateRequest( searchCompositeDto,
			variableId, String.valueOf(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()));
		this.studyEntryService.updateStudyEntriesProperty(studyId, requestDto);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.studyEntryValidator).validateStudyContainsEntries(studyId, searchCompositeDto.getItemIds());
		Mockito.verify(this.studyEntryValidator).validateStudyContainsEntries(studyId, searchCompositeDto.getItemIds());
		Mockito.verify(this.termValidator).validate(variableId);
		Mockito.verify(this.middlewareStudyEntryService).updateStudyEntriesProperty(requestDto);
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
		Mockito.when(this.datasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))))
			.thenReturn(datasetDTOS);

		final MeasurementVariable entryCodeVariable = new MeasurementVariable(TermId.ENTRY_CODE.getId());
		final MeasurementVariable observationUnitIdVariable = new MeasurementVariable(TermId.OBS_UNIT_ID.getId());
		final MeasurementVariable entryNoVariable = new MeasurementVariable(TermId.ENTRY_NO.getId());
		final MeasurementVariable designationVariable = new MeasurementVariable(TermId.DESIG.getId());
		final MeasurementVariable crossVariable = new MeasurementVariable(TermId.CROSS.getId());
		final MeasurementVariable gidVariable = new MeasurementVariable(TermId.GID.getId());

		final List<MeasurementVariable> measurementVariables = Lists
			.newArrayList(entryCodeVariable, observationUnitIdVariable, entryNoVariable, designationVariable,
				crossVariable, gidVariable);

		Mockito.when(this.datasetService.getObservationSetVariables(datasetId, Lists
			.newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId()))).thenReturn(measurementVariables);

		final List<MeasurementVariable> results = this.studyEntryService.getEntryDescriptorColumns(studyId);

		MatcherAssert.assertThat(results, IsCollectionWithSize.hasSize(8));
		MatcherAssert.assertThat(entryCodeVariable, IsIn.in(results));
		MatcherAssert.assertThat(entryNoVariable, IsIn.in(results));
		MatcherAssert.assertThat(designationVariable, IsIn.in(results));
		MatcherAssert.assertThat(crossVariable, IsIn.in(results));
		MatcherAssert.assertThat(gidVariable, IsIn.in(results));
		MatcherAssert.assertThat(new MeasurementVariable(TermId.GID_UNIT.getId()), IsIn.in(results));
		MatcherAssert.assertThat(new MeasurementVariable(TermId.GID_AVAILABLE_BALANCE.getId()), IsIn.in(results));
		MatcherAssert.assertThat(new MeasurementVariable(TermId.GID_ACTIVE_LOTS_COUNT.getId()), IsIn.in(results));
	}

	@Test
	public void testCreateStudyGermplasmListDuplicateEntries() {

		final GermplasmList germplasmList = new GermplasmList();
		final List<GermplasmListData> listData = this.duplicateListData();
		germplasmList.setListData(listData);

		final Integer studyId = this.random.nextInt();
		final Integer germplasmListId = this.random.nextInt();

		Mockito.when(this.germplasmListService.getGermplasmList(germplasmListId)).thenReturn(germplasmList);

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetId(datasetId);
		datasetDTO.setDatasetTypeId(DatasetTypeEnum.PLOT_DATA.getId());

		final List<StudyEntryDto> entryDtos = new ArrayList<>();
		for (final GermplasmListData gData : listData) {
			final StudyEntryDto entryDto = new StudyEntryDto();
			entryDto.setGid(gData.getGid());
			entryDto.setEntryCode(gData.getEntryCode());
			entryDto.setEntryId(gData.getEntryId());
			entryDtos.add(entryDto);
		}
		final List<DatasetDTO> datasetDTOS = Collections.singletonList(datasetDTO);
		Mockito.when(this.datasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))))
			.thenReturn(datasetDTOS);
		Mockito.when(this.middlewareStudyEntryService.saveStudyEntries(ArgumentMatchers.eq(studyId), ArgumentMatchers.anyList())).thenReturn(entryDtos);

		final List<StudyEntryDto> studyEntryDtos = this.studyEntryService.createStudyEntries(studyId, germplasmListId);
		Assert.assertNotNull("Duplicate gid in list should be accepted. ", studyEntryDtos);
		org.junit.Assert.assertEquals("Must return same germplasm list data count", listData.size(),studyEntryDtos.size());
		Mockito.verify(this.germplasmListValidator).validateGermplasmList(germplasmListId);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.studyEntryValidator).validateStudyAlreadyHasStudyEntries(studyId);
		Mockito.verify(this.middlewareStudyEntryService).saveStudyEntries(ArgumentMatchers.eq(studyId), ArgumentMatchers.anyList());
	}

	private List<GermplasmListData> duplicateListData() {
		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(1);

		final GermplasmListData data1 = new GermplasmListData();
		data1.setGermplasm(germplasm);
		data1.setGid(germplasm.getGid());
		data1.setEntryId(1);
		data1.setEntryCode(StringUtils.randomAlphanumeric(3));

		final GermplasmListData data2 = new GermplasmListData();
		data2.setGermplasm(germplasm);
		data2.setGid(germplasm.getGid());
		data2.setEntryId(2);
		data2.setEntryCode(StringUtils.randomAlphanumeric(3));

		final List<GermplasmListData> listData = new ArrayList<>();
		listData.add(data1);
		listData.add(data2);
		return listData;
	}

}
