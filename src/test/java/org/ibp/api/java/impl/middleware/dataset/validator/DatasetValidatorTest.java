package org.ibp.api.java.impl.middleware.dataset.validator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

public class DatasetValidatorTest extends ApiUnitTestBase {
	
	@Autowired
	private DatasetValidator datasetValidator;
	
	@Autowired
	private OntologyDataManager ontologyDataManager;
	
	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public OntologyDataManager ontologyDataManager() {
			return Mockito.mock(OntologyDataManager.class);
		}
	}
	
	@Test (expected = ResourceNotFoundException.class)
	public void testDatasetDoesNotExist() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		Mockito.when(studyDataManager.getDataSet(datasetId)).thenReturn(null);
		this.datasetValidator.validateDataset(ran.nextInt(), datasetId, ran.nextBoolean());
	}
	
	@Test (expected = NotSupportedException.class)
	public void testDatasetShouldBeSubobservationDatasetButWasNot() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
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
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		final StandardVariable standardVariable = createStandardVariable(variableId);
		createDataset(datasetId, programUUID, standardVariable);
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, programUUID)).thenReturn(standardVariable);
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(ran.nextInt(), datasetId, ran.nextBoolean(), datasetVariable, false);
	}

	@Test (expected = ApiRequestValidationException.class)
	public void testShouldBeDatasetVariableButItIsNot() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		createDataset(datasetId, programUUID, null);
		final StandardVariable standardVariable = createStandardVariable(variableId);
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, programUUID)).thenReturn(standardVariable);
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(ran.nextInt(), datasetId, ran.nextBoolean(), datasetVariable, true);
	}
	

	@Test
	public void testShouldNotBeDatasetVariable() {
		final Random ran = new Random();
		final Integer datasetId = ran.nextInt();
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
		final Integer variableId = ran.nextInt();
		final String programUUID = RandomStringUtils.randomAlphabetic(20);
		final StandardVariable standardVariable = createStandardVariable(variableId);
		createDataset(datasetId, programUUID, standardVariable);
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, programUUID)).thenReturn(standardVariable);
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(ran.nextInt(), datasetId, ran.nextBoolean(), datasetVariable, true);
	}
	
	private StandardVariable createStandardVariable(final Integer traitId) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(traitId);
		standardVariable.setVariableTypes(Collections.singleton(VariableType.SELECTION_METHOD));
		return standardVariable;
	}

	private void createDataset(final Integer datasetId, final String programUUID, final StandardVariable stdVariable) {
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
	}
			

}
