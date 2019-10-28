package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignTypeValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EntryListOrderDesignTypeServiceImplTest {

	public static final String PLOT_NO = "PLOT_NO";
	public static final String ENTRY_NO = "ENTRY_NO";

	@Mock
	public ExperimentDesignGenerator experimentDesignGenerator;

	@Mock
	public ExperimentDesignTypeValidator experimentDesignTypeValidator;

	@Mock
	public OntologyDataManager ontologyDataManager;

	@InjectMocks
	private final EntryListOrderDesignTypeServiceImpl designTypeService = new EntryListOrderDesignTypeServiceImpl();

	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphanumeric(10);

	@Test
	public void testGenerateDesign() {

		final List<ObservationUnitRow> observationUnitRowList = new ArrayList<>();

		final int studyId = 1;
		final int numberOfTreatments = 5;
		final int numberOfControls = 1;
		final Integer numberOfTrials = 1;
		final Integer startingPlotNumber = 1;
		final Integer checkStartingPosition = 0;

		final List<StudyGermplasmDto> studyGermplasmDtoList =
			StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(numberOfTreatments, numberOfControls);
		final ExperimentDesignInput experimentDesignInput = new ExperimentDesignInput();
		final Set<Integer> trialInstancesForDesignGeneration = new HashSet<>(Arrays.asList(1, 2, 3));
		experimentDesignInput.setTrialInstancesForDesignGeneration(trialInstancesForDesignGeneration);
		experimentDesignInput.setStartingPlotNo(startingPlotNumber);
		experimentDesignInput.setCheckStartingPosition(checkStartingPosition);

		when(this.experimentDesignGenerator
			.constructMeasurementVariables(studyId, PROGRAM_UUID, EntryListOrderDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				EntryListOrderDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES, experimentDesignInput))
			.thenReturn(this.createMeasurementVariables());

		final List<ObservationUnitRow> result =
			this.designTypeService.generateDesign(studyId, experimentDesignInput, PROGRAM_UUID, studyGermplasmDtoList);

		verify(this.experimentDesignTypeValidator).validateEntryListOrderDesign(experimentDesignInput, studyGermplasmDtoList);

		assertEquals(5, result.size());
		assertEquals(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
			result.get(0).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
		assertEquals(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
			result.get(1).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
		assertEquals(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
			result.get(2).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
		assertEquals(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
			result.get(3).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
		assertEquals(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
			result.get(4).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());

	}

	@Test
	public void testGenerateDesignWithCheckPlan() {

		final List<ObservationUnitRow> observationUnitRowList = new ArrayList<>();

		final int studyId = 1;
		final int numberOfTreatments = 5;
		final int numberOfControls = 1;
		final Integer checkStartingPosition = 1;
		final Integer checkSpacing = 2;
		final Integer checkInsertionManner = InsertionMannerItem.INSERT_EACH_IN_TURN.getId();
		final Integer numberOfTrials = 1;
		final Integer startingPlotNumber = 1;

		final List<StudyGermplasmDto> studyGermplasmDtoList =
			StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(numberOfTreatments, numberOfControls);
		final ExperimentDesignInput experimentDesignInput = new ExperimentDesignInput();
		experimentDesignInput.setCheckStartingPosition(checkStartingPosition);
		experimentDesignInput.setCheckSpacing(checkSpacing);
		experimentDesignInput.setCheckInsertionManner(checkInsertionManner);
		final Set<Integer> trialInstancesForDesignGeneration = new HashSet<>(Arrays.asList(1, 2, 3));
		experimentDesignInput.setTrialInstancesForDesignGeneration(trialInstancesForDesignGeneration);
		experimentDesignInput.setStartingPlotNo(startingPlotNumber);

		when(this.experimentDesignGenerator
			.constructMeasurementVariables(studyId, PROGRAM_UUID, EntryListOrderDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				EntryListOrderDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES_WITH_CHECK_PLAN, experimentDesignInput))
			.thenReturn(this.createMeasurementVariables());

		final List<ObservationUnitRow> result =
			this.designTypeService.generateDesign(studyId, experimentDesignInput, PROGRAM_UUID, studyGermplasmDtoList);

		verify(this.experimentDesignTypeValidator).validateEntryListOrderDesign(experimentDesignInput, studyGermplasmDtoList);

		assertEquals(8, result.size());
		assertEquals(String.valueOf(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()),
			result.get(0).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
		assertEquals(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
			result.get(1).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
		assertEquals(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
			result.get(2).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
		assertEquals(String.valueOf(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()),
			result.get(3).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
		assertEquals(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
			result.get(4).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
		assertEquals(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
			result.get(5).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
		assertEquals(String.valueOf(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()),
			result.get(6).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
		assertEquals(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
			result.get(7).getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());

	}

	List<StandardVariable> createTestStandardVariables() {
		final List<StandardVariable> standardVariables = new ArrayList<>();
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.PLOT_NO.getId(), PLOT_NO));
		return standardVariables;
	}

	List<MeasurementVariable> createMeasurementVariables() {
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.PLOT_NO.getId(), PLOT_NO));
		measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.ENTRY_TYPE.getId(), "ENTRY_TYPE"));
		measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.GID.getId(), "GID"));
		measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.DESIG.getId(), "DESIGNATION"));
		measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.ENTRY_NO.getId(), "ENTRY_NO"));
		measurementVariables
			.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.OBS_UNIT_ID.getId(), "OBS_UNIT_ID"));
		return measurementVariables;
	}

}
