package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
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
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

public class DatasetValidatorTest {

	private final String PROGRAM_UUID = RandomStringUtils.random(20);

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private DatasetService studyDatasetService;

	@Mock
	private DatasetTypeService datasetTypeService;

	@InjectMocks
	private DatasetValidator datasetValidator;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		ContextHolder.setCurrentProgram(this.PROGRAM_UUID);
		ContextHolder.setCurrentCrop("maize");

		final DatasetTypeDTO plotDatasetType = new DatasetTypeDTO(DatasetTypeEnum.PLOT_DATA.getId(), "PLOT_DATA");
		plotDatasetType.setObservationType(true);
		plotDatasetType.setSubObservationType(false);
		when(this.datasetTypeService.getDatasetTypeById(DatasetTypeEnum.PLOT_DATA.getId())).thenReturn(plotDatasetType);

		final DatasetTypeDTO quadratDatasetType =
			new DatasetTypeDTO(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId(), "QUADRAT_SUBOBSERVATIONS");
		quadratDatasetType.setObservationType(true);
		quadratDatasetType.setSubObservationType(true);
		when(this.datasetTypeService.getDatasetTypeById(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId())).thenReturn(quadratDatasetType);

	}

	@Test(expected = ResourceNotFoundException.class)
	public void testDatasetDoesNotExist() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(datasetId))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		when(this.studyDatasetService.getDataset(datasetId)).thenReturn(null);
		this.datasetValidator.validateDataset(studyId, datasetId);
	}

	@Test(expected = NotSupportedException.class)
	public void testDatasetShouldBeSubobservationDatasetButWasNot() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final DatasetDTO dataset = new DatasetDTO();
		dataset.setDatasetId(datasetId);
		dataset.setDatasetTypeId(DatasetTypeEnum.STUDY_CONDITIONS.getId());
		when(this.studyDatasetService.getDataset(datasetId)).thenReturn(dataset);
		when(this.studyDatasetService.allDatasetIdsBelongToStudy(studyId, Collections.singletonList(datasetId))).thenReturn(true);
		when(this.studyDatasetService.isValidDatasetId(datasetId)).thenReturn(true);

		final DatasetTypeDTO datasetType = new DatasetTypeDTO();
		datasetType.setObservationType(false);
		datasetType.setSubObservationType(false);
		when(this.datasetTypeService.getDatasetTypeById(dataset.getDatasetTypeId())).thenReturn(datasetType);
		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateObservationDatasetType(datasetId);
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
		dataset.setDatasetTypeId(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId());
		when(this.studyDatasetService.isValidDatasetId(datasetId)).thenReturn(true);
		when(this.studyDatasetService.getDataset(datasetId)).thenReturn(dataset);
		when(this.studyDatasetService.allDatasetIdsBelongToStudy(studyId, Collections.singletonList(datasetId))).thenReturn(true);

		this.datasetValidator.validateDataset(studyId, datasetId);
		this.datasetValidator.validateObservationDatasetType(datasetId);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testVariableTypeDoesNotExist() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		this.createDataset(studyId, datasetId, Optional.absent(), Optional.absent());

		final DatasetVariable datasetVariable = new DatasetVariable(ran.nextInt(), variableId, "");
		this.datasetValidator.validateDatasetVariable(studyId, datasetId, datasetVariable, ran.nextBoolean(), VariableType.TRAIT);
	}

	@Test(expected = NotSupportedException.class)
	public void testVariableTypeIsNotSupported() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		this.createDataset(studyId, datasetId, Optional.absent(), Optional.absent());

		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.ANALYSIS.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(studyId, datasetId, datasetVariable, ran.nextBoolean(), VariableType.ANALYSIS);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testVariableIsNotGivenVariableType() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final int variableId = ran.nextInt();
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(variableId);
		final HashSet<VariableType> types = new HashSet<>();
		types.add(VariableType.TRAIT);
		types.add(VariableType.ANALYSIS_SUMMARY);
		standardVariable.setVariableTypes(types);
		this.createDataset(studyId, datasetId, Optional.of(variableId), Optional.of(VariableType.TRAIT));
		when(this.ontologyDataManager.getStandardVariable(variableId, this.PROGRAM_UUID)).thenReturn(standardVariable);

		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(studyId, datasetId, datasetVariable, false, VariableType.SELECTION_METHOD);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testVariableIsGermplasmDescriptorAndStudyHasExperimentalDesign() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final int variableId = ran.nextInt();
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(variableId);
		final HashSet<VariableType> types = new HashSet<>();
		types.add(VariableType.GERMPLASM_DESCRIPTOR);
		standardVariable.setVariableTypes(types);
		this.createDataset(studyId, datasetId, Optional.of(variableId), Optional.of(VariableType.GERMPLASM_DESCRIPTOR));
		when(this.ontologyDataManager.getStandardVariable(variableId, this.PROGRAM_UUID)).thenReturn(standardVariable);
		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.GERMPLASM_DESCRIPTOR.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(studyId, datasetId, datasetVariable, false, VariableType.GERMPLASM_DESCRIPTOR);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testShouldNotBeDatasetVariableButItAlreadyIs() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final DatasetDTO dataset = this.createDataset(studyId, datasetId, Optional.of(variableId), Optional.of(VariableType.TRAIT));

		final Map<Integer, MeasurementVariable> measurementVariableMap =
			dataset.getVariables().stream().collect(Collectors.toMap(MeasurementVariable::getTermId, Function.identity()));
		this.datasetValidator.validateIfDatasetVariableAlreadyExists(variableId, false, measurementVariableMap, null);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testShouldBeDatasetVariableButItIsNot() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final DatasetDTO dataset = this.createDataset(studyId, datasetId, Optional.absent(), Optional.absent());
		final Map<Integer, MeasurementVariable> measurementVariableMap =
			dataset.getVariables().stream().collect(Collectors.toMap(MeasurementVariable::getTermId, Function.identity()));

		this.datasetValidator.validateIfDatasetVariableAlreadyExists(variableId, true, measurementVariableMap, null);
	}

	@Test(expected = NotSupportedException.class)
	public void testShouldBeDatasetVariableButVariableTypeNotSupported() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final DatasetDTO dataset = this.createDataset(studyId, datasetId, Optional.of(variableId), Optional.of(VariableType.TRAIT));
		final DatasetTypeDTO datasetType = new DatasetTypeDTO();
		datasetType.setObservationType(true);
		datasetType.setDatasetTypeId(dataset.getDatasetTypeId());
		dataset.getVariables().get(0).setVariableType(VariableType.ANALYSIS_SUMMARY);
		final Map<Integer, MeasurementVariable> measurementVariableMap =
			dataset.getVariables().stream().collect(Collectors.toMap(MeasurementVariable::getTermId, Function.identity()));

		this.datasetValidator.validateIfDatasetVariableAlreadyExists(variableId, true, measurementVariableMap, datasetType);
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
		final int variableId = ran.nextInt();
		final StandardVariable standardVariable = this.createStandardVariable(existingTraitId);
		this.createDataset(studyId, datasetId, Optional.of(existingTraitId), Optional.of(VariableType.TRAIT));
		when(this.ontologyDataManager.getStandardVariable(variableId, this.PROGRAM_UUID)).thenReturn(standardVariable);

		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(studyId, datasetId, datasetVariable, false, VariableType.SELECTION_METHOD);
	}

	@Test
	public void testShouldBeDatasetVariable() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final int variableId = ran.nextInt();
		final StandardVariable standardVariable = this.createStandardVariable(variableId);
		this.createDataset(studyId, datasetId, Optional.of(variableId), Optional.of(VariableType.TRAIT));
		when(this.ontologyDataManager.getStandardVariable(variableId, this.PROGRAM_UUID)).thenReturn(standardVariable);

		final DatasetVariable datasetVariable = new DatasetVariable(VariableType.SELECTION_METHOD.getId(), variableId, "");
		this.datasetValidator.validateDatasetVariable(studyId, datasetId, datasetVariable, true, VariableType.SELECTION_METHOD);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateExistingVariablesWhenOneVariableNotDatasetVariable() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		when(this.studyDatasetService.isValidDatasetId(datasetId)).thenReturn(true);
		Mockito.doReturn(Lists.newArrayList(new DatasetDTO(datasetId)))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final StandardVariable standardVariable = this.createStandardVariable(variableId);
		when(this.ontologyDataManager.getStandardVariable(variableId, this.PROGRAM_UUID)).thenReturn(standardVariable);
		this.createDataset(studyId, datasetId, Optional.of(variableId), Optional.of(VariableType.TRAIT));
		final int nonDatasetVariableId = ran.nextInt();
		final StandardVariable nonDatasetVariable = this.createStandardVariable(nonDatasetVariableId);
		when(this.ontologyDataManager.getStandardVariable(nonDatasetVariableId, this.PROGRAM_UUID)).thenReturn(nonDatasetVariable);

		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, Arrays.asList(variableId, nonDatasetVariableId));
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateExistingVariablesVariableIsGermplasmDescriptorAndStudyHasGeneratedDesign() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Integer datasetId = ran.nextInt();
		final DatasetDTO datasetDTO = new DatasetDTO(datasetId);
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setHasExperimentalDesign(true);
		datasetDTO.setInstances(Arrays.asList(new StudyInstance()));
		when(this.studyDatasetService.isValidDatasetId(datasetId)).thenReturn(true);
		Mockito.doReturn(Lists.newArrayList(datasetDTO))
			.when(this.studyDatasetService)
			.getDatasets(Matchers.anyInt(), Matchers.anySetOf(Integer.class));
		final Integer variableId = ran.nextInt();
		final StandardVariable standardVariable = this.createStandardVariable(variableId);
		standardVariable.setVariableTypes(Collections.singleton(VariableType.GERMPLASM_DESCRIPTOR));
		when(this.ontologyDataManager.getStandardVariable(variableId, this.PROGRAM_UUID)).thenReturn(standardVariable);
		this.createDataset(studyId, datasetId, Optional.of(variableId), Optional.of(VariableType.GERMPLASM_DESCRIPTOR));
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, Arrays.asList(variableId));
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateVariableBelongsToVariableType_Exception() {
		final Random ran = new Random();
		final int datasetId = ran.nextInt();
		final int variableId = ran.nextInt();
		when(this.studyDatasetService.getObservationSetVariables(datasetId, Arrays.asList(VariableType.ENVIRONMENT_CONDITION.getId())))
			.thenReturn(new ArrayList<>());
		this.datasetValidator.validateVariableBelongsToVariableType(datasetId, variableId, VariableType.ENVIRONMENT_CONDITION.getId());
	}

	public void testValidateVariableBelongsToVariableType_VariableExistsInTheSpecifiedVariableType() {
		final Random ran = new Random();
		final int datasetId = ran.nextInt();
		final int variableId = ran.nextInt();

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableId);
		when(this.studyDatasetService.getObservationSetVariables(datasetId, Arrays.asList(VariableType.ENVIRONMENT_CONDITION.getId())))
			.thenReturn(Arrays.asList(measurementVariable));
		this.datasetValidator.validateVariableBelongsToVariableType(datasetId, variableId, VariableType.ENVIRONMENT_CONDITION.getId());
	}

	private StandardVariable createStandardVariable(final Integer traitId) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(traitId);
		standardVariable.setVariableTypes(Collections.singleton(VariableType.SELECTION_METHOD));
		return standardVariable;
	}

	private DatasetDTO createDataset(final int studyId, final Integer datasetId, final Optional<Integer> variableId,
		final Optional<VariableType> variableTypeOptional) {
		final DatasetDTO dataset = new DatasetDTO();
		dataset.setDatasetId(datasetId);
		dataset.setDatasetTypeId(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId());

		final org.generationcp.middleware.service.impl.study.StudyInstance studyInstance = new StudyInstance();
		studyInstance.setHasExperimentalDesign(true);
		dataset.setInstances(Arrays.asList(studyInstance));

		final List<MeasurementVariable> variables = new ArrayList<>();
		if (variableId.isPresent() && variableTypeOptional.isPresent()) {
			final MeasurementVariable variable = new MeasurementVariable();
			variable.setVariableType(variableTypeOptional.get());
			variable.setTermId(variableId.get());
			variables.add(variable);
		}
		dataset.setVariables(variables);
		when(this.studyDatasetService.getDataset(datasetId)).thenReturn(dataset);
		when(this.studyDatasetService.isValidDatasetId(datasetId)).thenReturn(true);
		when(this.studyDatasetService.allDatasetIdsBelongToStudy(studyId, Collections.singletonList(datasetId))).thenReturn(true);

		return dataset;
	}

}
