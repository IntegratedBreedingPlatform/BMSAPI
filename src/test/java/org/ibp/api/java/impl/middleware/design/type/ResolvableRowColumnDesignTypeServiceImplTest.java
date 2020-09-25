package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentalDesignGeneratorTestDataUtil;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentalDesignProcessor;
import org.ibp.api.java.impl.middleware.design.generator.MeasurementVariableGenerator;
import org.ibp.api.java.impl.middleware.design.generator.ResolvableRowColumnDesignGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResolvableRowColumnDesignTypeServiceImplTest {

	private static final String ENTRY_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String PLOT_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String REP_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String ROW = RandomStringUtils.randomAlphabetic(10);
	private static final String COL = RandomStringUtils.randomAlphabetic(10);

	@Mock
	public ResolvableRowColumnDesignGenerator experimentDesignGenerator;

	@Mock
	public OntologyDataManager ontologyDataManager;

	@Mock
	private MeasurementVariableGenerator measurementVariableGenerator;

	@Mock
	private ExperimentalDesignProcessor experimentalDesignProcessor;

	@InjectMocks
	private final ResolvableRowColumnDesignTypeServiceImpl designTypeService = new ResolvableRowColumnDesignTypeServiceImpl();

	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphanumeric(10);

	@Before
	public void init() {

		when(this.ontologyDataManager
			.getStandardVariables(ResolvableRowColumnDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES, PROGRAM_UUID))
			.thenReturn(this.createTestStandardVariables());

	}

	@Test
	public void testGenerateDesign() {

		final MainDesign mainDesign = new MainDesign();
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final List<ObservationUnitRow> observationUnitRowList = new ArrayList<>();

		final int studyId = 1;
		final int numberOfTreatments = 10;
		final int numberOfControls = 5;
		final Integer replicationsCount = 6;
		final Integer rowPerReplications = 5;
		final Integer colPerReplications = 3;
		final Integer startingPlotNumber = 1;

		final List<StudyEntryDto> studyEntryDtoList =
			StudyEntryTestDataGenerator.createStudyEntryDtoList(numberOfTreatments, numberOfControls);
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(replicationsCount);
		experimentalDesignInput.setRowsPerReplications(rowPerReplications);
		experimentalDesignInput.setColsPerReplications(colPerReplications);
		experimentalDesignInput.setStartingPlotNo(startingPlotNumber);
		final Set<Integer> trialInstancesForDesignGeneration = new HashSet<>(Arrays.asList(1, 2, 3));
		experimentalDesignInput.setTrialInstancesForDesignGeneration(trialInstancesForDesignGeneration);
		experimentalDesignInput.setUseLatenized(false);

		when(this.experimentDesignGenerator
			.generate(experimentalDesignInput,
				ExperimentalDesignGeneratorTestDataUtil.getRowColVariablesMap(ROW, COL, PLOT_NO, ENTRY_NO, REP_NO),
				studyEntryDtoList.size(),
				null, null))
			.thenReturn(mainDesign);
		when(this.measurementVariableGenerator
			.generateFromExperimentalDesignInput(studyId, PROGRAM_UUID, ResolvableRowColumnDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				ResolvableRowColumnDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES, experimentalDesignInput))
			.thenReturn(measurementVariables);
		when(this.experimentalDesignProcessor
			.generateObservationUnitRows(eq(trialInstancesForDesignGeneration), refEq(measurementVariables),
				refEq(studyEntryDtoList), refEq(mainDesign),
				eq(ENTRY_NO),
				isNull(), any(Map.class))).thenReturn(observationUnitRowList);

		final List<ObservationUnitRow> result =
			this.designTypeService.generateDesign(studyId, experimentalDesignInput, PROGRAM_UUID, studyEntryDtoList);

		assertSame(result, observationUnitRowList);
	}

	@Test
	public void testGenerateDesignLatinized() {

		final MainDesign mainDesign = new MainDesign();
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final List<ObservationUnitRow> observationUnitRowList = new ArrayList<>();

		final int studyId = 1;
		final int numberOfTreatments = 10;
		final int numberOfControls = 5;
		final Integer replicationsCount = 6;
		final Integer rowPerReplications = 5;
		final Integer colPerReplications = 3;
		final Integer startingPlotNumber = 1;

		final List<StudyEntryDto> studyGermplasmDtoList =
			StudyEntryTestDataGenerator.createStudyEntryDtoList(numberOfTreatments, numberOfControls);
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(replicationsCount);
		experimentalDesignInput.setRowsPerReplications(rowPerReplications);
		experimentalDesignInput.setColsPerReplications(colPerReplications);
		experimentalDesignInput.setStartingPlotNo(startingPlotNumber);
		final Set<Integer> trialInstancesForDesignGeneration = new HashSet<>(Arrays.asList(1, 2, 3));
		experimentalDesignInput.setTrialInstancesForDesignGeneration(trialInstancesForDesignGeneration);

		// Set value 1 for Column Arrangement
		experimentalDesignInput.setReplicationsArrangement(1);
		experimentalDesignInput.setUseLatenized(true);

		final Map<BreedingViewVariableParameter, String> bvVariablesMap = new HashMap<>();
		bvVariablesMap.put(BreedingViewVariableParameter.ROW, ROW);
		bvVariablesMap.put(BreedingViewVariableParameter.COLUMN, COL);
		bvVariablesMap.put(BreedingViewVariableParameter.PLOT, PLOT_NO);
		bvVariablesMap.put(BreedingViewVariableParameter.ENTRY, ENTRY_NO);
		bvVariablesMap.put(BreedingViewVariableParameter.REP, REP_NO);
		when(this.experimentDesignGenerator
			.generate(experimentalDesignInput, bvVariablesMap, studyGermplasmDtoList.size(),
				null, null))
			.thenReturn(mainDesign);
		when(this.measurementVariableGenerator
			.generateFromExperimentalDesignInput(studyId, PROGRAM_UUID, ResolvableRowColumnDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				ResolvableRowColumnDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES_LATINIZED, experimentalDesignInput))
			.thenReturn(measurementVariables);
		when(this.experimentalDesignProcessor
			.generateObservationUnitRows(eq(trialInstancesForDesignGeneration), refEq(measurementVariables),
				refEq(studyGermplasmDtoList), refEq(mainDesign),
				eq(ENTRY_NO),
				isNull(), any(Map.class))).thenReturn(observationUnitRowList);

		final List<ObservationUnitRow> result =
			this.designTypeService.generateDesign(studyId, experimentalDesignInput, PROGRAM_UUID, studyGermplasmDtoList);

		assertSame(result, observationUnitRowList);
	}

	private List<StandardVariable> createTestStandardVariables() {
		final List<StandardVariable> standardVariables = new ArrayList<>();
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.ENTRY_NO.getId(), ENTRY_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.PLOT_NO.getId(), PLOT_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.REP_NO.getId(), REP_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.ROW.getId(), ROW));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.COL.getId(), COL));
		return standardVariables;
	}

}
