package org.ibp.api.java.impl.middleware.dataset.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DatasetValidatorTest  {

	@Mock
	protected StudyDataManager studyDataManager;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private DatasetService studyDatasetService;

	@InjectMocks
	private DatasetValidator datasetValidator;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
	}

	@Test (expected = ResourceNotFoundException.class)
	public void testDatasetDoesNotExist() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(datasetId))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		Mockito.when(studyDataManager.getDataSet(datasetId)).thenReturn(null);
		this.datasetValidator.validateDataset(ran.nextInt(), datasetId, ran.nextBoolean());
	}
	
	@Test (expected = NotSupportedException.class)
	public void testDatasetShouldBeSubobservationDatasetButWasNot() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final DataSet dataset = new DataSet();
		dataset.setId(datasetId);
		dataset.setDataSetType(DataSetType.PLOT_DATA);
		Mockito.when(studyDataManager.getDataSet(datasetId)).thenReturn(dataset);
		
		this.datasetValidator.validateDataset(ran.nextInt(), datasetId, true);
	}
	
	@Test
	public void testDatasetShouldBeSubobservationDataset() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final DataSet dataset = new DataSet();
		dataset.setId(datasetId);
		dataset.setDataSetType(DataSetType.QUADRAT_SUBOBSERVATIONS);
		Mockito.when(studyDataManager.getDataSet(datasetId)).thenReturn(dataset);
		
		this.datasetValidator.validateDataset(ran.nextInt(), datasetId, true);
	}
	
	@Test (expected = ResourceNotFoundException.class)
	public void testVariableDoesNotExist() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		createDataset(datasetId, programUUID, null);
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, programUUID)).thenReturn(null);
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.TRAIT.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(ran.nextInt(), datasetId, ran.nextBoolean(), datasetVariable, ran.nextBoolean());
	}
	
	@Test (expected = ResourceNotFoundException.class)
	public void testVariableTypeDoesNotExist() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		createDataset(datasetId, programUUID, null);
		
		final DatasetVariable datasetVariable = new DatasetVariable(ran.nextInt(), variableId, "");
		this.datasetValidator.validateDatasetVariable(ran.nextInt(), datasetId, ran.nextBoolean(), datasetVariable, ran.nextBoolean());
	}
	
	@Test (expected = NotSupportedException.class)
	public void testVariableTypeIsNotSupported() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		createDataset(datasetId, programUUID, null);
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.ANALYSIS.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(ran.nextInt(), datasetId, ran.nextBoolean(), datasetVariable, ran.nextBoolean());
	}
	
	@Test (expected = ApiRequestValidationException.class)
	public void testVariableIsNotGivenVariableType() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(variableId);
		final HashSet<VariableType> types = new HashSet<VariableType>();
		types.add(VariableType.TRAIT);
		types.add(VariableType.ANALYSIS_SUMMARY);
		standardVariable.setVariableTypes(types);
		createDataset(datasetId, programUUID, standardVariable);
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, programUUID)).thenReturn(standardVariable);
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(ran.nextInt(), datasetId, ran.nextBoolean(), datasetVariable, false);
	}
	
	@Test (expected = ApiRequestValidationException.class)
	public void testShouldNotBeDatasetVariableButItAlreadyIs() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		final StandardVariable standardVariable = createStandardVariable(variableId);
		final DataSet dataset = createDataset(datasetId, programUUID, standardVariable);
		
		this.datasetValidator.validateIfAlreadyDatasetVariable(variableId, false, dataset);
	}

	@Test (expected = ApiRequestValidationException.class)
	public void testShouldBeDatasetVariableButItIsNot() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		final DataSet dataset = createDataset(datasetId, programUUID, null);
		
		this.datasetValidator.validateIfAlreadyDatasetVariable(variableId, true, dataset);;
	}
	
	@Test (expected = NotSupportedException.class)
	public void testShouldBeDatasetVariableButVariableTypeNotSupported() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		final StandardVariable standardVariable = createStandardVariable(variableId);
		final DataSet dataset = createDataset(datasetId, programUUID, standardVariable);
		dataset.getVariableTypes().getVariableTypes().get(0).setVariableType(VariableType.ANALYSIS_SUMMARY);
		
		this.datasetValidator.validateIfAlreadyDatasetVariable(variableId, true, dataset);
	}
	

	@Test
	public void testShouldNotBeDatasetVariable() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer existingTraitId = ran.nextInt();
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		final StandardVariable standardVariable = createStandardVariable(existingTraitId);
		createDataset(datasetId, programUUID, standardVariable);
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, programUUID)).thenReturn(standardVariable);
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(ran.nextInt(), datasetId, ran.nextBoolean(), datasetVariable, false);
	}
	
	@Test
	public void testShouldBeDatasetVariable() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		final StandardVariable standardVariable = createStandardVariable(variableId);
		createDataset(datasetId, programUUID, standardVariable);
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, programUUID)).thenReturn(standardVariable);
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(ran.nextInt(), datasetId, ran.nextBoolean(), datasetVariable, true);
	}
	
	@Test (expected = ResourceNotFoundException.class)
	public void testValidateExistingVariablesWhenOneVariableDoesNotExist() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final Integer nonExistingVariableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		final StandardVariable standardVariable = createStandardVariable(variableId);
		createDataset(datasetId, programUUID, standardVariable);
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, programUUID)).thenReturn(standardVariable);
		Mockito.when(ontologyDataManager.getStandardVariable(nonExistingVariableId, programUUID)).thenReturn(null);
		
		this.datasetValidator.validateExistingDatasetVariables(ran.nextInt(), datasetId, true, Arrays.asList(variableId, nonExistingVariableId));
	}
	
	@Test (expected = ApiRequestValidationException.class)
	public void testValidateExistingVariablesWhenOneVariableNotDatasetVariable() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		final StandardVariable standardVariable = createStandardVariable(variableId);
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, programUUID)).thenReturn(standardVariable);
		createDataset(datasetId, programUUID, standardVariable);
		final Integer nonDatasetVariableId = ran.nextInt();
		final StandardVariable nonDatasetVariable = createStandardVariable(nonDatasetVariableId);
		Mockito.when(ontologyDataManager.getStandardVariable(nonDatasetVariableId, programUUID)).thenReturn(nonDatasetVariable);
		
		this.datasetValidator.validateExistingDatasetVariables(ran.nextInt(), datasetId, true, Arrays.asList(variableId, nonDatasetVariableId));
	}
	
	private StandardVariable createStandardVariable(final Integer traitId) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(traitId);
		standardVariable.setVariableTypes(Collections.singleton(VariableType.SELECTION_METHOD));
		return standardVariable;
	}

	private DataSet createDataset(final Integer datasetId, final String programUUID, final StandardVariable stdVariable) {
		final DataSet dataset = new DataSet();
		dataset.setId(datasetId);
		dataset.setProgramUUID(programUUID);
		dataset.setDataSetType(DataSetType.QUADRAT_SUBOBSERVATIONS);
		
		final VariableTypeList datasetVariables = new VariableTypeList();
		if (stdVariable != null){
			final DMSVariableType variable = new DMSVariableType();
			variable.setVariableType(VariableType.TRAIT);
			variable.setStandardVariable(stdVariable);
			variable.setRole(PhenotypicType.VARIATE);
			datasetVariables.setVariableTypes(Arrays.asList(variable));
		}
		dataset.setVariableTypes(datasetVariables);
		Mockito.when(studyDataManager.getDataSet(datasetId)).thenReturn(dataset);
		
		return dataset;
	}
			

}
