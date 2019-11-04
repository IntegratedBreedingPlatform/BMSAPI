package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.generator.MeasurementVariableGenerator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AugmentedRandomizedBlockDesignTypeServiceImplTest {

	public static final String ENTRY_NO = "ENTRY_NO";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String BLOCK_NO = "BLOCK_NO";

	@Mock
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Mock
	private MeasurementVariableGenerator measurementVariableGenerator;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@InjectMocks
	private final AugmentedRandomizedBlockDesignTypeServiceImpl designTypeService = new AugmentedRandomizedBlockDesignTypeServiceImpl();

	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphanumeric(10);

	@Before
	public void init() {

		when(this.ontologyDataManager
			.getStandardVariables(AugmentedRandomizedBlockDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES, PROGRAM_UUID))
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
		final Integer numberOfBlocks = 5;
		final Integer startingPlotNumber = 1;
		final Integer numberOfTrials = 3;

		final List<StudyGermplasmDto> studyGermplasmDtoList =
			StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(numberOfTreatments, numberOfControls);
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setNumberOfBlocks(numberOfBlocks);
		experimentalDesignInput.setStartingPlotNo(startingPlotNumber);
		final Set<Integer> trialInstancesForDesignGeneration = new HashSet<>(Arrays.asList(1, 2, 3));
		experimentalDesignInput.setTrialInstancesForDesignGeneration(trialInstancesForDesignGeneration);

		when(this.experimentDesignGenerator
			.createAugmentedRandomizedBlockDesign(numberOfBlocks, numberOfTreatments, numberOfControls,
				startingPlotNumber,
				ENTRY_NO,
				BLOCK_NO, PLOT_NO)).thenReturn(mainDesign);
		when(this.measurementVariableGenerator
			.generateFromExperimentalDesignInput(studyId, PROGRAM_UUID, AugmentedRandomizedBlockDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				AugmentedRandomizedBlockDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES, experimentalDesignInput))
			.thenReturn(measurementVariables);
		when(this.experimentDesignGenerator
			.generateObservationUnitRowsFromExperimentalDesign(eq(trialInstancesForDesignGeneration), refEq(measurementVariables),
				refEq(studyGermplasmDtoList), refEq(mainDesign),
				eq(ENTRY_NO),
				isNull(), any(Map.class))).thenReturn(observationUnitRowList);

		final List<ObservationUnitRow> result =
			this.designTypeService.generateDesign(studyId, experimentalDesignInput, PROGRAM_UUID, studyGermplasmDtoList);

		assertSame(result, observationUnitRowList);
	}

	@Test
	public void testCreateMapOfBreedingViewExpectedEntriesToGermplasmEntriesInStudy() {

		final int numberOfTestEntries = 3;
		final int numberOfCheckEntries = 2;
		final List<StudyGermplasmDto> studyGermplasmDtoList =
			StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(numberOfTestEntries, numberOfCheckEntries);

		final Set<Integer> entryIdsOfCheckEntries = this.designTypeService.getEntryIdsOfChecks(studyGermplasmDtoList);
		final Set<Integer> entryIdsOfTestEntries = this.designTypeService.getEntryIdsOfTestEntries(studyGermplasmDtoList);

		final Map<Integer, Integer> result = this.designTypeService
			.createMapOfDesignExpectedEntriesToGermplasmEntriesInTrial(studyGermplasmDtoList, entryIdsOfCheckEntries,
				entryIdsOfTestEntries);

		assertEquals("Entry Number 1 should be mapped to Test Entry", 3, result.get(1).intValue());
		assertEquals("Entry Number 2 should be mapped to Test Entry", 4, result.get(2).intValue());
		assertEquals("Entry Number 3 should be mapped to Test Entry", 5, result.get(3).intValue());
		assertEquals("Entry Number 4 should be mapped to Check Entry 1", 1, result.get(4).intValue());
		assertEquals("Entry Number 5 should be mapped to Check Entry 2", 2, result.get(5).intValue());

	}

	@Test
	public void testGetEntryIdsOfCheckEntries() {

		final int numberOfTestEntries = 3;
		final int numberOfCheckEntries = 2;
		final List<StudyGermplasmDto> studyGermplasmDtoList =
			StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(numberOfTestEntries, numberOfCheckEntries);

		final Set<Integer> result = this.designTypeService.getEntryIdsOfChecks(studyGermplasmDtoList);

		assertEquals("There are only 2 checks in the germplasm list", numberOfCheckEntries, result.size());

		// Entry Number 1 and 2 should exist in the result set.
		assertTrue(result.contains(1));
		assertTrue(result.contains(2));

	}

	@Test
	public void testGetEntryIdsOfTestEntries() {

		final int numberOfTestEntries = 3;
		final int numberOfCheckEntries = 2;
		final List<StudyGermplasmDto> studyGermplasmDtoList =
			StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(numberOfTestEntries, numberOfCheckEntries);

		final Set<Integer> result = this.designTypeService.getEntryIdsOfTestEntries(studyGermplasmDtoList);

		assertEquals("There should be 3 test entries in the list", numberOfTestEntries, result.size());

		// Entry Id 1 and 2 should not exist in the result set because they are check entries
		assertTrue(!result.contains(1));
		assertTrue(!result.contains(2));

	}

	List<StandardVariable> createTestStandardVariables() {
		final List<StandardVariable> standardVariables = new ArrayList<>();
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.ENTRY_NO.getId(), ENTRY_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.BLOCK_NO.getId(), BLOCK_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.PLOT_NO.getId(), PLOT_NO));
		return standardVariables;
	}

}
