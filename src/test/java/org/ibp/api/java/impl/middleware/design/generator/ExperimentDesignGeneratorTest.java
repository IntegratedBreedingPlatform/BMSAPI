package org.ibp.api.java.impl.middleware.design.generator;

import org.generationcp.commons.constant.AppConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.domain.design.ExpDesign;
import org.ibp.api.domain.design.ExpDesignParameter;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.exception.BVDesignException;
import org.ibp.api.java.design.runner.DesignRunner;
import org.ibp.api.java.impl.middleware.design.transformer.StandardVariableTransformer;
import org.ibp.api.java.impl.middleware.design.type.StudyGermplasmTestDataGenerator;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.BVDesignProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentDesignGeneratorTest {
	private static final String TRIAL_NO = "TRIAL_NO";
	public static final String BLOCK_NO = "BLOCK_NO";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String ENTRY_NO = "ENTRY_NO";
	public static final String REP_NO = "REP_NO";
	public static final Integer NBLOCK = 2;
	public static final String OUTPUT_FILE = "outputfile.csv";

	private static final int ENTRY_NO_9 = 9;
	private static final int ENTRY_NO_3 = 3;
	private static final int ENTRY_NO_10 = 10;
	private static final int ENTRY_NO_5 = 5;

	@InjectMocks
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Mock
	private DesignRunner designRunner;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private DatasetService datasetService;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private StandardVariableTransformer standardVariableTransformer;

	@Test
	public void testCreateRandomizedCompleteBlockDesign() {

		final List<String> treatmentFactors = new ArrayList<>(Arrays.asList("FACTOR_1", "FACTOR_2"));
		final List<String> levels = new ArrayList<>(Arrays.asList("Level1", "Level2"));
		final Integer initialPlotNumber = 99;

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createRandomizedCompleteBlockDesign(NBLOCK, BLOCK_NO, PLOT_NO, initialPlotNumber, TermId.ENTRY_NO.name(),
				treatmentFactors,
				levels, OUTPUT_FILE);

		final ExpDesign expDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.RANDOMIZED_COMPLETE_BLOCK_DESIGN, expDesign.getName());
		Assert.assertEquals("", expDesign.getParameterValue(ExperimentDesignGenerator.SEED_PARAM));
		Assert.assertEquals(String.valueOf(NBLOCK), expDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		Assert.assertEquals(BLOCK_NO, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, expDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(
			String.valueOf(initialPlotNumber),
			expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));
		Assert.assertEquals(
			treatmentFactors.size(),
			expDesign.getParameterList(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM).size());
		Assert.assertEquals(treatmentFactors.size(), expDesign.getParameterList(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM).size());
		Assert.assertEquals(levels.size(), expDesign.getParameterList(ExperimentDesignGenerator.LEVELS_PARAM).size());
		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			expDesign.getParameterValue(ExperimentDesignGenerator.TIMELIMIT_PARAM));
		Assert.assertEquals(OUTPUT_FILE, expDesign.getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM));

	}

	@Test
	public void testCreateResolvableIncompleteBlockDesign() {

		final Integer numberOfTreatments = 30;
		final Integer numberOfReplicates = 31;
		final Integer blockSize = 22;
		final Integer initialPlotNumber = 99;
		final Integer nBLatin = 1;
		final String replatinGroups = "sample1,sample2";

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createResolvableIncompleteBlockDesign(blockSize, numberOfTreatments, numberOfReplicates, ENTRY_NO, REP_NO, BLOCK_NO,
				PLOT_NO, initialPlotNumber, nBLatin, replatinGroups,  OUTPUT_FILE,false);

		final ExpDesign expDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, expDesign.getName());
		Assert.assertEquals("", expDesign.getParameterValue(ExperimentDesignGenerator.SEED_PARAM));
		Assert.assertEquals(String.valueOf(blockSize), expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKSIZE_PARAM));
		Assert.assertEquals(String.valueOf(numberOfTreatments), expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfReplicates), expDesign.getParameterValue(ExperimentDesignGenerator.NREPLICATES_PARAM));
		Assert.assertEquals(
			"1",
			expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(REP_NO, expDesign.getParameterValue(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, expDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(
			String.valueOf(initialPlotNumber),
			expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));

		Assert.assertEquals("0", expDesign.getParameterValue(ExperimentDesignGenerator.NBLATIN_PARAM));

		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			expDesign.getParameterValue(ExperimentDesignGenerator.TIMELIMIT_PARAM));
		Assert.assertEquals(OUTPUT_FILE, expDesign.getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM));

	}

	@Test
	public void testCreateResolvableIncompleteBlockDesignLatinized() {

		final Integer numberOfTreatments = 30;
		final Integer numberOfReplicates = 31;
		final Integer blockSize = 22;
		final Integer initialPlotNumber = 99;
		final Integer nBLatin = 1;
		final String replatinGroups = "sample1,sample2";

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createResolvableIncompleteBlockDesign(blockSize, numberOfTreatments, numberOfReplicates, ENTRY_NO, REP_NO, BLOCK_NO,
				PLOT_NO, initialPlotNumber, nBLatin, replatinGroups, OUTPUT_FILE, true);

		final ExpDesign expDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, expDesign.getName());
		Assert.assertEquals("", expDesign.getParameterValue(ExperimentDesignGenerator.SEED_PARAM));
		Assert.assertEquals(String.valueOf(blockSize), expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKSIZE_PARAM));
		Assert.assertEquals(String.valueOf(numberOfTreatments), expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfReplicates), expDesign.getParameterValue(ExperimentDesignGenerator.NREPLICATES_PARAM));
		Assert.assertEquals(
			"1",
			expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(REP_NO, expDesign.getParameterValue(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, expDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(
			String.valueOf(initialPlotNumber),
			expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));

		// Latinized Parameters
		Assert.assertEquals(String.valueOf(nBLatin), expDesign.getParameterValue(ExperimentDesignGenerator.NBLATIN_PARAM));
		Assert.assertEquals(2, expDesign.getParameterList(ExperimentDesignGenerator.REPLATINGROUPS_PARAM).size());

		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			expDesign.getParameterValue(ExperimentDesignGenerator.TIMELIMIT_PARAM));
		Assert.assertEquals(OUTPUT_FILE, expDesign.getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM));

	}

	@Test
	public void testCeateAugmentedRandomizedBlockDesign() {

		final Integer numberOfBlocks = 2;
		final Integer numberOfTreatments = 22;
		final Integer numberOfControls = 11;
		final Integer startingPlotNumber = 1;

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createAugmentedRandomizedBlockDesign(
				numberOfBlocks, numberOfTreatments, numberOfControls, startingPlotNumber, ENTRY_NO, BLOCK_NO, PLOT_NO);

		final ExpDesign expDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.AUGMENTED_RANDOMIZED_BLOCK_DESIGN, expDesign.getName());
		Assert.assertEquals(String.valueOf(numberOfTreatments), expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfControls), expDesign.getParameterValue(ExperimentDesignGenerator.NCONTROLS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfBlocks), expDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		Assert.assertEquals(ENTRY_NO, expDesign.getParameterValue(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, expDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(
			"1",
			expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(
			String.valueOf(startingPlotNumber),
			expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));
	}

	@Test
	public void testCreatePRepDesign() {

		final Integer numberOfBlocks = 2;
		final Integer numberOfTreatments = 22;
		final Integer startingPlotNumber = 1;
		final List<ListItem> nRepeatsListItem = Arrays.asList(new ListItem("1"));

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createPRepDesign(
				numberOfBlocks, numberOfTreatments, nRepeatsListItem, ENTRY_NO, BLOCK_NO, PLOT_NO, startingPlotNumber);

		final ExpDesign expDesign = mainDesign.getDesign();

		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			expDesign.getParameterValue(ExperimentDesignGenerator.TIMELIMIT_PARAM));
		Assert.assertEquals(String.valueOf(numberOfTreatments), expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfBlocks), expDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		Assert.assertNull(expDesign.getParameterValue(ExperimentDesignGenerator.NREPEATS_PARAM));
		Assert.assertSame(nRepeatsListItem, expDesign.getParameterList(ExperimentDesignGenerator.NREPEATS_PARAM));
		Assert.assertEquals(ENTRY_NO, expDesign.getParameterValue(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, expDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(
			String.valueOf(1),
			expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(
			String.valueOf(startingPlotNumber),
			expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));
		Assert.assertEquals(ExperimentDesignGenerator.P_REP_DESIGN, expDesign.getName());

	}

	@Test
	public void testCreateReplicationListItemForPRepDesignNoCheckEntries() {

		final int noOfTestEntries = 5;
		final int replicationNumber = 3;
		final float replicationPercentage = 50.0f;
		final float noOfTestEntriesToReplicate = Math.round((float) noOfTestEntries * (replicationPercentage / 100));

		final List<StudyGermplasmDto> importedGermplasmList = StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(5, 0);
		final List<ListItem> listItems =
			this.experimentDesignGenerator
				.createReplicationListItemForPRepDesign(importedGermplasmList, replicationPercentage, replicationNumber);

		float countOfReplicatedListItem = 0;
		for (final ListItem listItem : listItems) {
			if (listItem.getValue().equals(String.valueOf(replicationNumber))) {
				countOfReplicatedListItem++;
			}
		}

		Assert.assertEquals(String.valueOf(countOfReplicatedListItem), String.valueOf(noOfTestEntriesToReplicate));

	}

	@Test
	public void testCreateReplicationListItemForPRepDesignWithSystemDefinedCheckEntryType() {

		final int noOfTestEntries = 4;
		final int noOfCheckEntries = 1;
		final int replicationNumber = 3;
		final float replicationPercentage = 50.0f;
		final float noOfTestEntriesToReplicate = Math.round((float) noOfTestEntries * (replicationPercentage / 100));

		final List<StudyGermplasmDto> importedGermplasmList = StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(5, 0);

		// Set the first germplasm as CHECK_ENTRY.
		final StudyGermplasmDto checkImportedGermplasm = importedGermplasmList.get(0);
		checkImportedGermplasm.setCheckType(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());

		final List<ListItem> listItems =
			this.experimentDesignGenerator
				.createReplicationListItemForPRepDesign(importedGermplasmList, replicationPercentage, replicationNumber);

		float countOfReplicatedListItem = 0;
		for (final ListItem listItem : listItems) {
			if (listItem.getValue().equals(String.valueOf(replicationNumber))) {
				countOfReplicatedListItem++;
			}
		}

		Assert.assertEquals(String.valueOf(countOfReplicatedListItem), String.valueOf(noOfTestEntriesToReplicate + noOfCheckEntries));

	}

	@Test
	public void testCreateReplicationListItemForPRepDesignWithCustomEntryType() {

		final int noOfTestEntries = 4;
		final int noOfCheckEntries = 1;
		final int replicationNumber = 3;
		final float replicationPercentage = 50.0f;
		final float noOfTestEntriesToReplicate = Math.round((float) noOfTestEntries * (replicationPercentage / 100));

		final List<StudyGermplasmDto> importedGermplasmList = StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(5, 0);

		// Set the first germplasm as CUSTOM ENTRY_TYPE.
		final StudyGermplasmDto checkImportedGermplasm = importedGermplasmList.get(0);
		// Any custom entry type (cagetorical id not in SystemDefineEntryType) is considered as check type.
		final int customEntryTypeCategoricalId = 1000;
		checkImportedGermplasm.setCheckType(customEntryTypeCategoricalId);

		final List<ListItem> listItems =
			this.experimentDesignGenerator
				.createReplicationListItemForPRepDesign(importedGermplasmList, replicationPercentage, replicationNumber);

		float countOfReplicatedListItem = 0;
		for (final ListItem listItem : listItems) {
			if (listItem.getValue().equals(String.valueOf(replicationNumber))) {
				countOfReplicatedListItem++;
			}
		}

		Assert.assertEquals(String.valueOf(countOfReplicatedListItem), String.valueOf(noOfTestEntriesToReplicate + noOfCheckEntries));

	}

	@Test
	public void testAddInitialTreatmenNumberIfAvailableInitialEntryNumberIsNull() {

		final List<ExpDesignParameter> paramList = new ArrayList<>();

		final Integer initialEntryNumber = null;

		this.experimentDesignGenerator.addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

		Assert.assertEquals("Initial Treatment Number param should not be added to the param list.", 0, paramList.size());

	}

	@Test
	public void testAddInitialTreatmenNumberIfAvailableInitialEntryNumberHasValue() {

		final List<ExpDesignParameter> paramList = new ArrayList<>();

		final Integer initialEntryNumber = 2;

		this.experimentDesignGenerator.addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

		Assert.assertEquals("Initial Treatment Number param should  be added to the param list.", 1, paramList.size());
		Assert.assertEquals(String.valueOf(initialEntryNumber), paramList.get(0).getValue());

	}

	@Test
	public void testConvertToListItemList() {

		final List<String> listOfString = new LinkedList<>();

		final String sampleText1 = "sample text 1";
		final String sampleText2 = "sample text 2";

		listOfString.add(sampleText1);
		listOfString.add(sampleText2);

		final List<ListItem> listItems = this.experimentDesignGenerator.convertToListItemList(listOfString);

		Assert.assertEquals(2, listItems.size());
		Assert.assertEquals(sampleText1, listItems.get(0).getValue());
		Assert.assertEquals(sampleText2, listItems.get(1).getValue());

	}

	@Test
	public void testGetPlotNumberStringValue() {

		Assert.assertEquals("If the initialPlotNumber is null, it should return the default plot number which is '1'.", "1",
			this.experimentDesignGenerator.getPlotNumberStringValueOrDefault(null));
		Assert.assertEquals("99", this.experimentDesignGenerator.getPlotNumberStringValueOrDefault(99));
	}


	@Test
	public void testGetInitialTreatNumList() {
		final List<String> treatmentFactors = Arrays.asList(TermId.ENTRY_NO.name(), "NFERT_NO");
		final List<ListItem> listItems = this.experimentDesignGenerator.getInitialTreatNumList(treatmentFactors, 5, TermId.ENTRY_NO.name());
		Assert.assertEquals("5", listItems.get(0).getValue());
		Assert.assertEquals("1", listItems.get(1).getValue());
	}

	@Test
	public void testResolveMappedEntryNumber() {

		final Map<Integer, Integer> designExpectedEntriesMap = new HashMap<>();
		// Test Entry No 9 is mapped to Check Entry No 3
		designExpectedEntriesMap.put(ENTRY_NO_9, ENTRY_NO_3);
		// Test Entry No 10 is mapped to Check Entry No 5
		designExpectedEntriesMap.put(ENTRY_NO_10, ENTRY_NO_5);

		final Integer result1 =
			this.experimentDesignGenerator.resolveMappedEntryNumber(9, designExpectedEntriesMap);
		Assert.assertEquals("Lookup value 9 should return 3", Integer.valueOf(3), result1);

		final Integer result2 =
			this.experimentDesignGenerator.resolveMappedEntryNumber(10, designExpectedEntriesMap);
		Assert.assertEquals("Lookup value 10 should return 5", Integer.valueOf(5), result2);

		final Integer result5 =
			this.experimentDesignGenerator.resolveMappedEntryNumber(9999, designExpectedEntriesMap);
		Assert.assertEquals("9999 is not in map of checks, the return value should be the same number", Integer.valueOf(9999), result5);
	}


	@Test
	public void testGenerateExperimentDesignMeasurements() throws IOException, BVDesignException {
		final MainDesign mainDesign =
			this.experimentDesignGenerator
				.createRandomizedCompleteBlockDesign(2, ExperimentDesignGeneratorTest.REP_NO, ExperimentDesignGeneratorTest.PLOT_NO,
					301, TermId.ENTRY_NO.name(), new ArrayList<>(), new ArrayList<>(), "");

		final int environments = 7;
		this.setMockValues(mainDesign, environments);

		final Map<String, List<String>> treatmentFactorValues = new HashMap<>();

		final List<StudyGermplasmDto> studyGermplasmDtoList = StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(5, 0);
		final List<ObservationUnitRow> measurementRowList =
			this.experimentDesignGenerator
				.generateExperimentDesignMeasurements(environments, new ArrayList<>(), studyGermplasmDtoList,
					mainDesign, ExperimentDesignGeneratorTest.ENTRY_NO,
					treatmentFactorValues, new HashMap<>());

		Assert.assertEquals(
			String.valueOf(environments),
			mainDesign.getDesign().getParameterValue(ExperimentDesignGenerator.NUMBER_TRIALS_PARAM));
		Mockito.verify(this.designRunner, Mockito.times(1)).runBVDesign(mainDesign);
	}

	@Test
	public void testGenerateMeasurementsBVDesignError() throws IOException {
		final MainDesign mainDesign =
			this.experimentDesignGenerator
				.createRandomizedCompleteBlockDesign(2, ExperimentDesignGeneratorTest.REP_NO, ExperimentDesignGeneratorTest.PLOT_NO,
					301,  TermId.ENTRY_NO.name(), new ArrayList<>(), new ArrayList<>(), "");
		Mockito.doReturn(new BVDesignOutput(1)).when(this.designRunner)
			.runBVDesign(mainDesign);
		try {
			final List<StudyGermplasmDto> studyGermplasmDtoList = StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(5, 0);
			this.experimentDesignGenerator
					.generateExperimentDesignMeasurements(2, new ArrayList<>(), studyGermplasmDtoList,
						mainDesign, ExperimentDesignGeneratorTest.ENTRY_NO,	new HashMap<>(), new HashMap<>());
			Assert.fail("Expected to throw BVDesignException but didn't.");
		} catch (final BVDesignException e) {
			Assert.assertEquals("experiment.design.generate.generic.error", e.getBvErrorCode());
		}
	}

	@Test
	public void testGenerateMeasurementsBVDesignIOException() throws IOException {
		final MainDesign mainDesign =
			this.experimentDesignGenerator
				.createRandomizedCompleteBlockDesign(2, ExperimentDesignGeneratorTest.REP_NO, ExperimentDesignGeneratorTest.PLOT_NO,
					301, TermId.ENTRY_NO.name(), new ArrayList<>(), new ArrayList<>(), "");
		Mockito.doThrow(new IOException()).when(this.designRunner)
			.runBVDesign(mainDesign);
		try {
			final List<StudyGermplasmDto> studyGermplasmDtoList = StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(5, 0);
			this.experimentDesignGenerator
				.generateExperimentDesignMeasurements(2, new ArrayList<>(), studyGermplasmDtoList,
					mainDesign, ExperimentDesignGeneratorTest.ENTRY_NO,	new HashMap<>(), new HashMap<>());
			Assert.fail("Expected to throw BVDesignException but didn't.");
		} catch (final BVDesignException e) {
			Assert.assertEquals("experiment.design.bv.exe.error.generate.generic.error", e.getBvErrorCode());
		}
	}

	private void setMockValues(final MainDesign design, final int numberOfInstances) throws IOException {
		Mockito.when(this.designRunner.runBVDesign(design))
			.thenReturn(this.createBvOutput(numberOfInstances));
	}

	private BVDesignOutput createBvOutput(int numberOfInstances) {
		final BVDesignOutput bvOutput = new BVDesignOutput(0);
		bvOutput.setResults(this.createEntries(numberOfInstances, 2, 5));
		return bvOutput;
	}

	private List<StandardVariable> createRequiredVariables() {
		final List<StandardVariable> reqVariables = new ArrayList<>();

		reqVariables.add(this.createStandardVariable(TermId.REP_NO.getId(), ExperimentDesignGeneratorTest.REP_NO));
		reqVariables.add(this.createStandardVariable(TermId.PLOT_NO.getId(), ExperimentDesignGeneratorTest.PLOT_NO));

		return reqVariables;
	}

	private StandardVariable createStandardVariable(final int id, final String name) {
		final StandardVariable var = new StandardVariable();
		var.setId(id);
		var.setName(name);
		return var;
	}

	private List<String[]> createEntries(final Integer numOfInstances, final Integer numberofReps, final Integer numberOfEntries) {
		final List<String[]> entries = new ArrayList<String[]>();
		final String[] headers = new String[] {TRIAL_NO, PLOT_NO, REP_NO,
			ENTRY_NO};

		entries.add(headers);
		for (int i = 1; i <= numOfInstances; i++) {
			final String trial = String.valueOf(i);
			int plotNo = 100;
			for (int j = 1; j <= numberofReps; j++) {
				for (int k = 1; k <= numberOfEntries; k++) {
					final String plot = String.valueOf(plotNo++);
					final String[] data = new String[] {trial, plot, String.valueOf(j), String.valueOf(k)};
					entries.add(data);
				}
			}
		}
		return entries;
	}

}
