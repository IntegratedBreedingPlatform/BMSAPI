package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentalDesignTypeValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RandomizeCompleteBlockDesignTypeServiceImplTest {

	private static final int TREATMENT_FACTOR_KEY_VARIABLE = 8241;
	private static final int TREATMENT_FACTOR_LABEL_VARIABLE = 8260;
	public static final String ENTRY_NO = "ENTRY_NO";
	public static final String PLOT_NO = "PLOT_NO";
	private static final String REP_NO = "REP_NO";

	@Mock
	public ExperimentalDesignTypeValidator experimentalDesignTypeValidator;

	@Mock
	public ExperimentDesignGenerator experimentDesignGenerator;

	@Mock
	public HibernateSessionProvider sessionProvider;

	@Mock
	public OntologyDataManager ontologyDataManager;

	@InjectMocks
	private final RandomizeCompleteBlockDesignTypeServiceImpl designTypeService = new RandomizeCompleteBlockDesignTypeServiceImpl();

	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphanumeric(10);

	@Before
	public void init() {
		when(this.ontologyDataManager
			.getStandardVariables(RandomizeCompleteBlockDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES, PROGRAM_UUID))
			.thenReturn(this.createTestStandardVariables());
		when(this.ontologyDataManager
			.getStandardVariables(Arrays.asList(TREATMENT_FACTOR_LABEL_VARIABLE, TREATMENT_FACTOR_KEY_VARIABLE), PROGRAM_UUID))
			.thenReturn(this.createTestStandardVariablesForTreatmentFactors());

	}

	@Test
	public void testGenerateDesign() {
		final MeasurementVariableTransformer transformer = new MeasurementVariableTransformer(this.sessionProvider);
		this.designTypeService.setMeasurementVariableTransformer(transformer);

		final MainDesign mainDesign = new MainDesign();
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final List<ObservationUnitRow> observationUnitRowList = new ArrayList<>();

		final int studyId = 1;
		final int numberOfTreatments = 10;
		final int numberOfControls = 5;
		final int replicationCount = 5;
		final int startingPlotNumber = 1;
		final int numberOfTrials = 1;

		final List<StudyGermplasmDto> studyGermplasmDtoList =
			StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(numberOfTreatments, numberOfControls);
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(replicationCount);
		experimentalDesignInput.setStartingPlotNo(startingPlotNumber);
		final Set<Integer> trialInstancesForDesignGeneration = new HashSet<>(Arrays.asList(1, 2, 3));
		experimentalDesignInput.setTrialInstancesForDesignGeneration(trialInstancesForDesignGeneration);
		experimentalDesignInput.setTreatmentFactorsData(this.createTreatmentFactorsDataMap());

		when(this.experimentDesignGenerator
			.createRandomizedCompleteBlockDesign(eq(replicationCount), eq(REP_NO), eq(PLOT_NO),
				eq(startingPlotNumber),
				eq(ENTRY_NO), any(List.class), any(List.class), eq(""))).thenReturn(mainDesign);
		when(this.experimentDesignGenerator
			.constructMeasurementVariables(studyId, PROGRAM_UUID, RandomizeCompleteBlockDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				RandomizeCompleteBlockDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES, experimentalDesignInput))
			.thenReturn(measurementVariables);
		when(this.experimentDesignGenerator
			.generateExperimentDesignMeasurements(eq(trialInstancesForDesignGeneration), refEq(measurementVariables),
				refEq(studyGermplasmDtoList), refEq(mainDesign),
				eq(ENTRY_NO),
				any(Map.class), any(Map.class))).thenReturn(observationUnitRowList);

		final List<ObservationUnitRow> result =
			this.designTypeService.generateDesign(studyId, experimentalDesignInput, PROGRAM_UUID, studyGermplasmDtoList);

		assertSame(result, observationUnitRowList);
		verify(this.experimentalDesignTypeValidator).validateRandomizedCompleteBlockDesign(experimentalDesignInput, studyGermplasmDtoList);

	}

	@Test
	public void testGetTreatmentFactors() {
		final List<String> result = this.designTypeService
			.getTreatmentFactors(this.designTypeService.getTreatmentFactorValues(this.createTreatmentFactorsDataMap()));
		assertEquals(Arrays.asList("_8241"), result);
	}

	@Test
	public void testGetLevels() {
		final List<String> result = this.designTypeService
			.getLevels(this.designTypeService.getTreatmentFactorValues(this.createTreatmentFactorsDataMap()));
		assertEquals(Arrays.asList("1"), result);
	}

	Map createTreatmentFactorsDataMap() {

		final Map treatmentFactorsMap = new HashMap();
		final Map parentTermValuesMap = new HashMap();
		parentTermValuesMap.put("levels", "1");
		parentTermValuesMap.put("labels", Arrays.asList("1000"));
		parentTermValuesMap.put("variableId", TREATMENT_FACTOR_LABEL_VARIABLE);
		treatmentFactorsMap.put(String.valueOf(TREATMENT_FACTOR_KEY_VARIABLE), parentTermValuesMap);
		return treatmentFactorsMap;

	}

	List<StandardVariable> createTestStandardVariables() {
		final List<StandardVariable> standardVariables = new ArrayList<>();
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.ENTRY_NO.getId(), ENTRY_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.REP_NO.getId(), REP_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.PLOT_NO.getId(), PLOT_NO));
		return standardVariables;
	}

	List<StandardVariable> createTestStandardVariablesForTreatmentFactors() {
		final List<StandardVariable> standardVariables = new ArrayList<>();
		standardVariables.add(
			StandardVariableTestDataInitializer.createStandardVariable(TREATMENT_FACTOR_KEY_VARIABLE, "TREATMENT_FACTOR_KEY_VARIABLE"));
		standardVariables.add(
			StandardVariableTestDataInitializer.createStandardVariable(TREATMENT_FACTOR_LABEL_VARIABLE, "TREATMENT_FACTOR_LABEL_VARIABLE"));
		return standardVariables;
	}

}
