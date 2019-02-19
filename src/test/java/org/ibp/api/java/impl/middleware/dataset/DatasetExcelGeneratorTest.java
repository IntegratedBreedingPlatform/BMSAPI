package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.DatasetType;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.hamcrest.Matchers;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class DatasetExcelGeneratorTest {
	private static final Integer STUDY_ID = 1;
	private ResourceBundleMessageSource messageSource;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private DatasetService datasetService;

	@InjectMocks
	private DatasetXLSGenerator datasetExcelGenerator;

	@Before
	public void setUp() {
		this.messageSource = new ResourceBundleMessageSource();
		messageSource.setUseCodeAsDefaultMessage(true);
		this.datasetExcelGenerator.setMessageSource(this.messageSource);
		final DataSet dataSet = new DataSet();
		dataSet.setId(DatasetExcelGeneratorTest.STUDY_ID);
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyTypeDto.getTrialDto());
		Mockito.when(this.studyDataManager.getStudyDetails(DatasetExcelGeneratorTest.STUDY_ID)).thenReturn(studyDetails);
		Mockito.when(this.studyDataManager.getDataSetsByType(DatasetExcelGeneratorTest.STUDY_ID, DataSetType.SUMMARY_DATA)).thenReturn(Arrays.asList(dataSet));
	}

	@Test
	public void testGenerateSingleInstanceFile() throws IOException {
		final String filename = "filename";
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceDbId(1);
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetTypeId(DataSetType.PLANT_SUBOBSERVATIONS.getId());
		datasetDTO.setDatasetId(1);
		datasetDTO.setParentDatasetId(1);
		final File
			file = this.datasetExcelGenerator.generateSingleInstanceFile(DatasetExcelGeneratorTest.STUDY_ID, datasetDTO, new ArrayList<MeasurementVariable>(), new ArrayList<ObservationUnitRow>(), filename, studyInstance);
		Assert.assertEquals(filename, file.getName());
		Mockito.verify(this.studyDataManager).getStudyDetails(1);
		Mockito.verify(this.datasetService).getMeasurementVariables(DatasetExcelGeneratorTest.STUDY_ID, Lists.newArrayList(VariableType.STUDY_DETAIL.getId()));
		Mockito.verify(this.studyDataManager).getDataSetsByType(DatasetExcelGeneratorTest.STUDY_ID, DataSetType.SUMMARY_DATA);
		Mockito.verify(this.datasetService)
			.getMeasurementVariables(
				1, Lists
					.newArrayList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
						VariableType.STUDY_CONDITION.getId(), VariableType.TRAIT.getId()));
		Mockito.verify(this.datasetService).getMeasurementVariables(1, Lists
			.newArrayList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.TREATMENT_FACTOR.getId(),
				VariableType.GERMPLASM_DESCRIPTOR.getId()));
		Mockito.verify(this.datasetService)
			.getMeasurementVariables(1, Lists
				.newArrayList(VariableType.OBSERVATION_UNIT.getId(), VariableType.TRAIT.getId(), VariableType.SELECTION_METHOD.getId()));
		Mockito.verify(this.studyDataManager).getPhenotypeByVariableId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
		Mockito.verify(this.studyDataManager).getGeolocationByVariableId(1, 1);
	}
}
