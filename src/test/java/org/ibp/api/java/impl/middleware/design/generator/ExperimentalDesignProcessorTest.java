package org.ibp.api.java.impl.middleware.design.generator;

import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.exception.BVDesignException;
import org.ibp.api.java.design.runner.DesignRunner;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.type.StudyEntryTestDataGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentalDesignProcessorTest {

	private static final int ENTRY_NO_9 = 9;
	private static final int ENTRY_NO_3 = 3;
	private static final int ENTRY_NO_10 = 10;
	private static final int ENTRY_NO_5 = 5;

	private static final String TRIAL_NO = "TRIAL_NO";
	private static final String PLOT_NO = "PLOT_NO";
	private static final String ENTRY_NO = "ENTRY_NO";
	private static final String REP_NO = "REP_NO";

	private static final int NUMBER_OF_REPS = 2;
	private static final int NUMBER_OF_ENTRIES = 5;


	@Mock
	private DesignRunner designRunner;

	@InjectMocks
	private ExperimentalDesignProcessor experimentalDesignProcessor;

	private ExperimentalDesignInput designInput;

	@Before
	public void init() {
		this.designInput = new ExperimentalDesignInput();
		this.designInput.setNumberOfBlocks(2);
		this.designInput.setStartingPlotNo(301);
	}

	@Test
	public void testResolveMappedEntryNumber() {

		final Map<Integer, Integer> designExpectedEntriesMap = new HashMap<>();
		// Test Entry No 9 is mapped to Check Entry No 3
		designExpectedEntriesMap.put(ENTRY_NO_9, ENTRY_NO_3);
		// Test Entry No 10 is mapped to Check Entry No 5
		designExpectedEntriesMap.put(ENTRY_NO_10, ENTRY_NO_5);

		final Integer result1 =
			this.experimentalDesignProcessor.resolveMappedEntryNumber(9, designExpectedEntriesMap);
		Assert.assertEquals("Lookup value 9 should return 3", Integer.valueOf(3), result1);

		final Integer result2 =
			this.experimentalDesignProcessor.resolveMappedEntryNumber(10, designExpectedEntriesMap);
		Assert.assertEquals("Lookup value 10 should return 5", Integer.valueOf(5), result2);

		final Integer result5 =
			this.experimentalDesignProcessor.resolveMappedEntryNumber(9999, designExpectedEntriesMap);
		Assert.assertEquals("9999 is not in map of checks, the return value should be the same number", Integer.valueOf(9999), result5);
	}

	@Test
	public void testGenerateObservationUnitRows() throws BVDesignException, IOException {
		final MainDesign mainDesign =
			new RandomizeCompleteBlockDesignGenerator()
				.generate(this.designInput, ExperimentalDesignGeneratorTestDataUtil.getRCBDVariablesMap(REP_NO, PLOT_NO),
					null, null, Collections.emptyMap());

		final List<Integer> trialInstances = Arrays.asList(2, 4, 6);
		final Set<Integer> trialInstancesForDesignGeneration = new HashSet<>(trialInstances);
		this.setMockValues(mainDesign, trialInstancesForDesignGeneration.size());

		final Map<String, List<String>> treatmentFactorValues = new HashMap<>();

		final List<StudyEntryDto> studyEntryDtoList = StudyEntryTestDataGenerator.createStudyEntryDtoList(5, 0);
		final List<ObservationUnitRow> measurementRowList =
			this.experimentalDesignProcessor
				.generateObservationUnitRows(trialInstancesForDesignGeneration, new ArrayList<>(), studyEntryDtoList,
					mainDesign, ENTRY_NO,
					treatmentFactorValues, new HashMap<>());

		final Map<Integer, List<ObservationUnitRow>> instancesRowMap = new HashMap<>();
		for (final ObservationUnitRow row : measurementRowList) {
			final Integer trialInstance = row.getTrialInstance();
			instancesRowMap.putIfAbsent(trialInstance, new ArrayList<>());
			instancesRowMap.get(trialInstance).add(row);
		}
		Assert.assertEquals(trialInstances, new ArrayList<>(instancesRowMap.keySet()));
		Assert.assertEquals(NUMBER_OF_REPS * NUMBER_OF_ENTRIES, instancesRowMap.get(2).size());
		Assert.assertEquals(NUMBER_OF_REPS * NUMBER_OF_ENTRIES, instancesRowMap.get(4).size());
		Assert.assertEquals(NUMBER_OF_REPS * NUMBER_OF_ENTRIES, instancesRowMap.get(6).size());

		Assert.assertEquals(
			String.valueOf(trialInstancesForDesignGeneration.size()),
			mainDesign.getDesign().getParameterValue(BreedingViewDesignParameter.NUMBER_TRIALS.getParameterName()));
		Mockito.verify(this.designRunner, Mockito.times(1)).runBVDesign(mainDesign);
	}

	@Test
	public void testGenerateMeasurementsBVDesignError() throws IOException {
		final MainDesign mainDesign =
			new RandomizeCompleteBlockDesignGenerator()
				.generate(this.designInput, ExperimentalDesignGeneratorTestDataUtil.getRCBDVariablesMap(REP_NO, PLOT_NO),
					null, null, Collections.emptyMap());
		Mockito.doReturn(new BVDesignOutput(1)).when(this.designRunner)
			.runBVDesign(mainDesign);
		final Set<Integer> trialInstancesForDesignGeneration = new HashSet<>(Arrays.asList(1, 2));

		try {
			final List<StudyEntryDto> studyGermplasmDtoList = StudyEntryTestDataGenerator.createStudyEntryDtoList(5, 0);
			this.experimentalDesignProcessor
				.generateObservationUnitRows(trialInstancesForDesignGeneration, new ArrayList<>(), studyGermplasmDtoList,
					mainDesign, ENTRY_NO,	new HashMap<>(), new HashMap<>());
			Assert.fail("Expected to throw BVDesignException but didn't.");
		} catch (final BVDesignException e) {
			Assert.assertEquals("experiment.design.generate.generic.error", e.getBvErrorCode());
		}
	}

	@Test
	public void testGenerateMeasurementsBVDesignIOException() throws IOException {
		final MainDesign mainDesign =
			new RandomizeCompleteBlockDesignGenerator()
				.generate(this.designInput, ExperimentalDesignGeneratorTestDataUtil.getRCBDVariablesMap(REP_NO, PLOT_NO),
					null, null, Collections.emptyMap());
		Mockito.doThrow(new IOException()).when(this.designRunner)
			.runBVDesign(mainDesign);
		try {
			final List<StudyEntryDto> studyGermplasmDtoList = StudyEntryTestDataGenerator.createStudyEntryDtoList(5, 0);
			this.experimentalDesignProcessor
				.generateObservationUnitRows(new HashSet<>(Collections.singletonList(2)), new ArrayList<>(), studyGermplasmDtoList,
					mainDesign, ENTRY_NO,	new HashMap<>(), new HashMap<>());
			Assert.fail("Expected to throw BVDesignException but didn't.");
		} catch (final BVDesignException e) {
			Assert.assertEquals("experiment.design.bv.exe.error.generate.generic.error", e.getBvErrorCode());
		}
	}

	private void setMockValues(final MainDesign design, final int numberOfInstances) throws IOException {
		Mockito.when(this.designRunner.runBVDesign(design))
			.thenReturn(this.createBvOutput(numberOfInstances));
	}

	private BVDesignOutput createBvOutput(final int numberOfInstances) {
		final BVDesignOutput bvOutput = new BVDesignOutput(0);
		bvOutput.setResults(this.createEntries(numberOfInstances));
		return bvOutput;
	}

	private List<String[]> createEntries(final Integer numOfInstances) {
		final List<String[]> entries = new ArrayList<>();
		final String[] headers = new String[] {TRIAL_NO, PLOT_NO, REP_NO,
			ENTRY_NO};

		entries.add(headers);
		for (int i = 1; i <= numOfInstances; i++) {
			final String trial = String.valueOf(i);
			int plotNo = 100;
			for (int j = 1; j <= NUMBER_OF_REPS; j++) {
				for (int k = 1; k <= NUMBER_OF_ENTRIES; k++) {
					final String plot = String.valueOf(plotNo++);
					final String[] data = new String[] {trial, plot, String.valueOf(j), String.valueOf(k)};
					entries.add(data);
				}
			}
		}
		return entries;
	}

}
