package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class DatasetValidatorTest  {

	private final String PROGRAM_UUID = RandomStringUtils.random(20);

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private DatasetService studyDatasetService;
	
	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private DatasetValidator datasetValidator;
	

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
	}

	@Test (expected = ResourceNotFoundException.class)
	public void testDatasetDoesNotExist() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(datasetId))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		Mockito.when(this.studyDatasetService.getDataset(datasetId)).thenReturn(null);
		this.datasetValidator.validateDataset(studyId, datasetId, ran.nextBoolean());
	}
	
	@Test (expected = NotSupportedException.class)
	public void testDatasetShouldBeSubobservationDatasetButWasNot() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final DatasetDTO dataset = new DatasetDTO();
		dataset.setDatasetId(datasetId);
		dataset.setDatasetTypeId(DataSetType.PLOT_DATA.getId());
		Mockito.when(this.studyDatasetService.getDataset(datasetId)).thenReturn(dataset);
		
		this.datasetValidator.validateDataset(studyId, datasetId, true);
	}
	
	@Test
	public void testDatasetShouldBeSubobservationDataset() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final DatasetDTO dataset = new DatasetDTO();
		dataset.setDatasetId(datasetId);
		dataset.setDatasetTypeId(DataSetType.QUADRAT_SUBOBSERVATIONS.getId());
		Mockito.when(this.studyDatasetService.getDataset(datasetId)).thenReturn(dataset);
		
		this.datasetValidator.validateDataset(studyId, datasetId, true);
	}
	
	@Test (expected = ResourceNotFoundException.class)
	public void testVariableTypeDoesNotExist() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		createDataset(studyId, datasetId, Optional.<Integer>absent());
		
		final DatasetVariable datasetVariable = new DatasetVariable(ran.nextInt(), variableId, "");
		this.datasetValidator.validateDatasetVariable(studyId, datasetId, ran.nextBoolean(), datasetVariable, ran.nextBoolean());
	}
	
	@Test (expected = NotSupportedException.class)
	public void testVariableTypeIsNotSupported() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		createDataset(studyId, datasetId, Optional.<Integer>absent());
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.ANALYSIS.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(studyId, datasetId, ran.nextBoolean(), datasetVariable, ran.nextBoolean());
	}
	
	@Test (expected = ApiRequestValidationException.class)
	public void testVariableIsNotGivenVariableType() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(variableId);
		final HashSet<VariableType> types = new HashSet<VariableType>();
		types.add(VariableType.TRAIT);
		types.add(VariableType.ANALYSIS_SUMMARY);
		standardVariable.setVariableTypes(types);
		createDataset(studyId, datasetId, Optional.of(variableId));
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, PROGRAM_UUID)).thenReturn(standardVariable);
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(studyId, datasetId, ran.nextBoolean(), datasetVariable, false);
	}
	
	@Test (expected = ApiRequestValidationException.class)
	public void testShouldNotBeDatasetVariableButItAlreadyIs() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final DatasetDTO dataset = createDataset(studyId, datasetId,  Optional.of(variableId));
		
		this.datasetValidator.validateIfAlreadyDatasetVariable(variableId, false, dataset);
	}

	@Test (expected = ApiRequestValidationException.class)
	public void testShouldBeDatasetVariableButItIsNot() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final DatasetDTO dataset = createDataset(studyId, datasetId, Optional.<Integer>absent());
		
		this.datasetValidator.validateIfAlreadyDatasetVariable(variableId, true, dataset);;
	}
	
	@Test (expected = NotSupportedException.class)
	public void testShouldBeDatasetVariableButVariableTypeNotSupported() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final DatasetDTO dataset = createDataset(studyId, datasetId, Optional.of(variableId));
		dataset.getVariables().get(0).setVariableType(VariableType.ANALYSIS_SUMMARY);
		
		this.datasetValidator.validateIfAlreadyDatasetVariable(variableId, true, dataset);
	}
	

	@Test
	public void testShouldNotBeDatasetVariable() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer existingTraitId = ran.nextInt();
		final Integer variableId = ran.nextInt();
		final StandardVariable standardVariable = createStandardVariable(existingTraitId);
		createDataset(studyId, datasetId, Optional.of(existingTraitId));
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, PROGRAM_UUID)).thenReturn(standardVariable);
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(studyId, datasetId, ran.nextBoolean(), datasetVariable, false);
	}
	
	@Test
	public void testShouldBeDatasetVariable() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final StandardVariable standardVariable = createStandardVariable(variableId);
		createDataset(studyId, datasetId, Optional.of(variableId));
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, PROGRAM_UUID)).thenReturn(standardVariable);
		
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(studyId, datasetId, ran.nextBoolean(), datasetVariable, true);
	}
	
	@Test (expected = ApiRequestValidationException.class)
	public void testValidateExistingVariablesWhenOneVariableNotDatasetVariable() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final StandardVariable standardVariable = createStandardVariable(variableId);
		Mockito.when(ontologyDataManager.getStandardVariable(variableId, PROGRAM_UUID)).thenReturn(standardVariable);
		createDataset(studyId, datasetId, Optional.of(variableId));
		final Integer nonDatasetVariableId = ran.nextInt();
		final StandardVariable nonDatasetVariable = createStandardVariable(nonDatasetVariableId);
		Mockito.when(ontologyDataManager.getStandardVariable(nonDatasetVariableId, PROGRAM_UUID)).thenReturn(nonDatasetVariable);
		
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, true, Arrays.asList(variableId, nonDatasetVariableId));
	}
	
	private StandardVariable createStandardVariable(final Integer traitId) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(traitId);
		standardVariable.setVariableTypes(Collections.singleton(VariableType.SELECTION_METHOD));
		return standardVariable;
	}

	private DatasetDTO createDataset(final Integer studyId, final Integer datasetId, final Optional<Integer> variableId) {
		final DatasetDTO dataset = new DatasetDTO();
		dataset.setDatasetId(datasetId);
		dataset.setDatasetTypeId(DataSetType.QUADRAT_SUBOBSERVATIONS.getId());
		
		final List<MeasurementVariable> variables = new ArrayList<>();
		if (variableId.isPresent()){
			final MeasurementVariable variable = new MeasurementVariable();
			variable.setVariableType(VariableType.TRAIT);
			variable.setTermId(variableId.get());
			variables.add(variable);
		}
		dataset.setVariables(variables);
		Mockito.when(this.studyDatasetService.getDataset(datasetId)).thenReturn(dataset);
		
		return dataset;
	}
			

}
