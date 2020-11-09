package org.ibp.api.java.impl.middleware.study;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
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
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsIn;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
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
import org.springframework.util.Assert;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
	private GermplasmListValidator germplasmListValidator;

	@Mock
	private org.ibp.api.java.germplasm.GermplamListService germplasmListService;

	@Mock
	private TermValidator termValidator;

	@Mock
	private StudyEntryService middlewareStudyEntryService;

	@Mock
	private DatasetService datasetService;

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
	public void testCreateStudyGermplasmList() {

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
		Mockito.verify(this.studyEntryValidator).validateStudyAlreadyHasStudyEntries(studyId);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.middlewareStudyEntryService).saveStudyEntries(ArgumentMatchers.eq(studyId), ArgumentMatchers.anyList());

	}

	@Test
	public void testDeleteStudyGermplasm() {
		final Integer studyId = this.random.nextInt();
		this.studyEntryService.deleteStudyEntries(studyId);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.middlewareStudyEntryService).deleteStudyEntries(studyId);
	}

	@Test
	public void testUpdateStudyEntryProperty() {

		final Integer studyId = this.random.nextInt();
		final Integer entryId = this.random.nextInt();
		final Integer variableId = this.random.nextInt();
		final Integer studyEntryPropertyId = this.random.nextInt();
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

		try {
			final List<StudyEntryDto> studyEntryDtos = this.studyEntryService.createStudyEntries(studyId, germplasmListId);
			Assert.notNull(studyEntryDtos, "Duplicate gid in list should be accepted. ");
			org.junit.Assert.assertEquals("Must return same germplasm list data count", listData.size(),studyEntryDtos.size());
		} catch (final Exception e) {
			Assert.isNull(e, "Duplicate gid in list should be accepted, no exception");
		}

		Mockito.verify(this.germplasmListValidator).validateGermplasmList(germplasmListId);
		Mockito.verify(this.studyEntryValidator).validateStudyAlreadyHasStudyEntries(studyId);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.middlewareStudyEntryService).saveStudyEntries(ArgumentMatchers.eq(studyId), ArgumentMatchers.anyList());
	}

	private List<GermplasmListData> duplicateListData() {
		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(1);

		final GermplasmListData data1 = new GermplasmListData();
		data1.setGermplasm(germplasm);
		data1.setGid(germplasm.getGid());
		data1.setEntryId(this.randomEntryId());
		data1.setEntryCode(StringUtils.randomAlphanumeric(3));

		final GermplasmListData data2 = new GermplasmListData();
		data2.setGermplasm(germplasm);
		data2.setGid(germplasm.getGid());
		data2.setEntryId(this.randomEntryId());
		data2.setEntryCode(StringUtils.randomAlphanumeric(3));

		final List<GermplasmListData> listData = new ArrayList<>();
		listData.add(data1);
		listData.add(data2);
		return listData;
	}

	private int randomEntryId() {
		int entryId;
		try {
			final double random = Math.random() * 100;
			entryId = (int) random;
		} catch (final Exception e) {
			entryId = 1;
		}
		return entryId;
	}

}
