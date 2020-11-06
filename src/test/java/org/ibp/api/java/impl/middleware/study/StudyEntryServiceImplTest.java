package org.ibp.api.java.impl.middleware.study;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyEntryGeneratorRequestDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsIn;
import org.ibp.api.java.impl.middleware.common.validator.EntryTypeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.common.SearchRequestDtoResolver;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
	private org.ibp.api.java.germplasm.GermplamListService germplasmListService;

	@Mock
	private TermValidator termValidator;

	@Mock
	private StudyEntryService middlewareStudyEntryService;

	@Mock
	private DatasetService datasetService;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@InjectMocks
	private final StudyEntryServiceImpl studyEntryService = new StudyEntryServiceImpl();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	private final Random random = new Random();

	@Test
	public void testReplaceStudyEntry() {
		final Integer studyId = random.nextInt();
		final Integer entryId = random.nextInt();
		final Integer newGid = random.nextInt();
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
		final Integer studyId = random.nextInt();
		final Integer gid = random.nextInt();
		final List<Integer> gids = Collections.singletonList(gid);
		final StudyEntryGeneratorRequestDto studyEntryGeneratorRequestDto = new StudyEntryGeneratorRequestDto();
		studyEntryGeneratorRequestDto.setEntryTypeId(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
		final SearchCompositeDto searchCompositeDto = new SearchCompositeDto();
		searchCompositeDto.setItemIds(new HashSet(gids));
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
		List<GermplasmListData> listData = new ArrayList<>();
		germplasmList.setListData(listData);

		final Integer studyId = random.nextInt();
		final Integer germplasmListId = random.nextInt();

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
		Mockito.verify(this.middlewareStudyEntryService).saveStudyEntries(ArgumentMatchers.eq(studyId), ArgumentMatchers.anyList());

	}

	@Test
	public void testDeleteStudyGermplasm() {
		final Integer studyId = random.nextInt();
		this.studyEntryService.deleteStudyEntries(studyId);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.middlewareStudyEntryService).deleteStudyEntries(studyId);
	}

	@Test
	public void testUpdateStudyEntryProperty() {

		final Integer studyId = random.nextInt();
		final Integer entryId = random.nextInt();
		final Integer variableId = random.nextInt();
		final Integer studyEntryPropertyId = random.nextInt();
		final StudyEntryPropertyData studyEntryPropertyData = new StudyEntryPropertyData();
		studyEntryPropertyData.setVariableId(variableId);
		studyEntryPropertyData.setStudyEntryPropertyId(studyEntryPropertyId);
		this.studyEntryService.updateStudyEntryProperty(studyId, entryId, studyEntryPropertyData);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.studyValidator).validateStudyContainsEntry(studyId, entryId);
		Mockito.verify(this.termValidator).validate(variableId);
		Mockito.verify(this.studyEntryValidator).validateStudyEntryProperty(studyEntryPropertyId);
		Mockito.verify(this.middlewareStudyEntryService).updateStudyEntryProperty(studyId, studyEntryPropertyData);
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

		final List<MeasurementVariable> results = studyEntryService.getEntryDescriptorColumns(studyId);

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

}
