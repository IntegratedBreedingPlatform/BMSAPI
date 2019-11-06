package org.ibp.api.java.impl.middleware.design.generator;

import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.design.ExperimentDesign;
import org.ibp.api.domain.design.ExperimentDesignParameter;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.type.StudyGermplasmTestDataGenerator;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentDesignGeneratorTest {

	private static final String BLOCK_NO = "BLOCK_NO";
	private static final String PLOT_NO = "PLOT_NO";
	private static final String ENTRY_NO = "ENTRY_NO";
	private static final String REP_NO = "REP_NO";
	private static final Integer NBLOCK = 2;

	@InjectMocks
	private ExperimentDesignGenerator experimentDesignGenerator;



	@Test
	public void testCreateRandomizedCompleteBlockDesign() {

		final List<String> treatmentFactors = new ArrayList<>(Arrays.asList("FACTOR_1", "FACTOR_2"));
		final List<String> levels = new ArrayList<>(Arrays.asList("Level1", "Level2"));
		final Integer initialPlotNumber = 99;

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setNumberOfBlocks(NBLOCK);
		experimentalDesignInput.setStartingPlotNo(initialPlotNumber);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createRandomizedCompleteBlockDesign(experimentalDesignInput, BLOCK_NO, PLOT_NO, TermId.ENTRY_NO.name(),
				treatmentFactors,
				levels);

		final ExperimentDesign experimentDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.RANDOMIZED_COMPLETE_BLOCK_DESIGN, experimentDesign.getName());
		Assert.assertEquals("", experimentDesign.getParameterValue(ExperimentDesignGenerator.SEED_PARAM));
		Assert.assertEquals(String.valueOf(NBLOCK), experimentDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		Assert.assertEquals(BLOCK_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(
			String.valueOf(initialPlotNumber),
			experimentDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));
		Assert.assertEquals(
			treatmentFactors.size(),
			experimentDesign.getParameterList(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM).size());
		Assert.assertEquals(treatmentFactors.size(), experimentDesign.getParameterList(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM).size());
		Assert.assertEquals(levels.size(), experimentDesign.getParameterList(ExperimentDesignGenerator.LEVELS_PARAM).size());
		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			experimentDesign.getParameterValue(ExperimentDesignGenerator.TIMELIMIT_PARAM));
	}

	@Test
	public void testCreateResolvableIncompleteBlockDesign() {

		final Integer numberOfTreatments = 30;
		final Integer numberOfReplicates = 31;
		final Integer blockSize = 22;
		final Integer initialPlotNumber = 99;
		final Integer nBLatin = 1;
		final String replatinGroups = "sample1,sample2";

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setBlockSize(blockSize);
		experimentalDesignInput.setReplicationsCount(numberOfReplicates);
		experimentalDesignInput.setStartingPlotNo(initialPlotNumber);
		experimentalDesignInput.setNblatin(nBLatin);
		experimentalDesignInput.setReplatinGroups(replatinGroups);
		experimentalDesignInput.setUseLatenized(false);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createResolvableIncompleteBlockDesign(experimentalDesignInput, numberOfTreatments, ENTRY_NO, REP_NO, BLOCK_NO,
				PLOT_NO);

		final ExperimentDesign experimentDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, experimentDesign.getName());
		Assert.assertEquals("", experimentDesign.getParameterValue(ExperimentDesignGenerator.SEED_PARAM));
		Assert.assertEquals(String.valueOf(blockSize), experimentDesign.getParameterValue(ExperimentDesignGenerator.BLOCKSIZE_PARAM));
		Assert.assertEquals(String.valueOf(numberOfTreatments), experimentDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfReplicates), experimentDesign.getParameterValue(ExperimentDesignGenerator.NREPLICATES_PARAM));
		Assert.assertEquals(
			"1",
			experimentDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(REP_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(
			String.valueOf(initialPlotNumber),
			experimentDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));

		Assert.assertEquals("0", experimentDesign.getParameterValue(ExperimentDesignGenerator.NBLATIN_PARAM));

		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			experimentDesign.getParameterValue(ExperimentDesignGenerator.TIMELIMIT_PARAM));
	}

	@Test
	public void testCreateResolvableIncompleteBlockDesignLatinized() {

		final Integer numberOfTreatments = 30;
		final Integer numberOfReplicates = 31;
		final Integer blockSize = 22;
		final Integer initialPlotNumber = 99;
		final Integer nBLatin = 1;
		final String replatinGroups = "sample1,sample2";

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setBlockSize(blockSize);
		experimentalDesignInput.setReplicationsCount(numberOfReplicates);
		experimentalDesignInput.setStartingPlotNo(initialPlotNumber);
		experimentalDesignInput.setNblatin(nBLatin);
		experimentalDesignInput.setReplatinGroups(replatinGroups);
		experimentalDesignInput.setUseLatenized(true);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createResolvableIncompleteBlockDesign(experimentalDesignInput, numberOfTreatments, ENTRY_NO, REP_NO, BLOCK_NO,
				PLOT_NO);

		final ExperimentDesign experimentDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, experimentDesign.getName());
		Assert.assertEquals("", experimentDesign.getParameterValue(ExperimentDesignGenerator.SEED_PARAM));
		Assert.assertEquals(String.valueOf(blockSize), experimentDesign.getParameterValue(ExperimentDesignGenerator.BLOCKSIZE_PARAM));
		Assert.assertEquals(String.valueOf(numberOfTreatments), experimentDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfReplicates), experimentDesign.getParameterValue(ExperimentDesignGenerator.NREPLICATES_PARAM));
		Assert.assertEquals(
			"1",
			experimentDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(REP_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(
			String.valueOf(initialPlotNumber),
			experimentDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));

		// Latinized Parameters
		Assert.assertEquals(String.valueOf(nBLatin), experimentDesign.getParameterValue(ExperimentDesignGenerator.NBLATIN_PARAM));
		Assert.assertEquals(2, experimentDesign.getParameterList(ExperimentDesignGenerator.REPLATINGROUPS_PARAM).size());

		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			experimentDesign.getParameterValue(ExperimentDesignGenerator.TIMELIMIT_PARAM));
	}

	@Test
	public void testCreateAugmentedRandomizedBlockDesign() {

		final Integer numberOfBlocks = 2;
		final Integer numberOfTreatments = 22;
		final Integer numberOfControls = 11;
		final Integer startingPlotNumber = 1;

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setNumberOfBlocks(numberOfBlocks);
		experimentalDesignInput.setStartingPlotNo(startingPlotNumber);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createAugmentedRandomizedBlockDesign(
				experimentalDesignInput, numberOfTreatments, numberOfControls, ENTRY_NO, BLOCK_NO, PLOT_NO);

		final ExperimentDesign experimentDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.AUGMENTED_RANDOMIZED_BLOCK_DESIGN, experimentDesign.getName());
		Assert.assertEquals(String.valueOf(numberOfTreatments), experimentDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfControls), experimentDesign.getParameterValue(ExperimentDesignGenerator.NCONTROLS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfBlocks), experimentDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		Assert.assertEquals(ENTRY_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(
			"1",
			experimentDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(
			String.valueOf(startingPlotNumber),
			experimentDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));
	}

	@Test
	public void testCreatePRepDesign() {

		final Integer numberOfBlocks = 2;
		final Integer numberOfTreatments = 22;
		final Integer startingPlotNumber = 1;
		final List<ListItem> nRepeatsListItem = Collections.singletonList(new ListItem("1"));

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setNumberOfBlocks(numberOfBlocks);
		experimentalDesignInput.setStartingPlotNo(startingPlotNumber);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.createPRepDesign(experimentalDesignInput,
				numberOfTreatments, nRepeatsListItem, ENTRY_NO, BLOCK_NO, PLOT_NO);

		final ExperimentDesign experimentDesign = mainDesign.getDesign();

		Assert.assertEquals(
			AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
			experimentDesign.getParameterValue(ExperimentDesignGenerator.TIMELIMIT_PARAM));
		Assert.assertEquals(String.valueOf(numberOfTreatments), experimentDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfBlocks), experimentDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		Assert.assertNull(experimentDesign.getParameterValue(ExperimentDesignGenerator.NREPEATS_PARAM));
		Assert.assertSame(nRepeatsListItem, experimentDesign.getParameterList(ExperimentDesignGenerator.NREPEATS_PARAM));
		Assert.assertEquals(ENTRY_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, experimentDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(
			String.valueOf(1),
			experimentDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(
			String.valueOf(startingPlotNumber),
			experimentDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));
		Assert.assertEquals(ExperimentDesignGenerator.P_REP_DESIGN, experimentDesign.getName());

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

		final List<ExperimentDesignParameter> paramList = new ArrayList<>();

		final Integer initialEntryNumber = null;

		this.experimentDesignGenerator.addInitialTreatmentNumberIfAvailable(initialEntryNumber, paramList);

		Assert.assertEquals("Initial Treatment Number param should not be added to the param list.", 0, paramList.size());

	}

	@Test
	public void testAddInitialTreatmenNumberIfAvailableInitialEntryNumberHasValue() {

		final List<ExperimentDesignParameter> paramList = new ArrayList<>();

		final Integer initialEntryNumber = 2;

		this.experimentDesignGenerator.addInitialTreatmentNumberIfAvailable(initialEntryNumber, paramList);

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











}
