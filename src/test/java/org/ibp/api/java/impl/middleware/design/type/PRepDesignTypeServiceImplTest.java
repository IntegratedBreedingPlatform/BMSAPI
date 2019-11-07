package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentalDesignProcessor;
import org.ibp.api.java.impl.middleware.design.generator.MeasurementVariableGenerator;
import org.ibp.api.java.impl.middleware.design.generator.PRepDesignGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PRepDesignTypeServiceImplTest {

	private static final String ENTRY_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String PLOT_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String BLOCK_NO = RandomStringUtils.randomAlphabetic(10);

	@Mock
	public PRepDesignGenerator experimentDesignGenerator;

	@Mock
	public OntologyDataManager ontologyDataManager;

	@Mock
	private MeasurementVariableGenerator measurementVariableGenerator;

	@Mock
	private ExperimentalDesignProcessor experimentalDesignProcessor;

	@InjectMocks
	private final PRepDesignTypeServiceImpl designTypeService = new PRepDesignTypeServiceImpl();

	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphanumeric(10);

	@Before
	public void init() {

		when(this.ontologyDataManager
			.getStandardVariables(PRepDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES, PROGRAM_UUID))
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
		final Integer blockSize = 5;
		final Integer replicationCount = 3;
		final Integer replicationPercentage = 50;
		final Integer startingPlotNumber = 1;

		final List<StudyGermplasmDto> studyGermplasmDtoList =
			StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(numberOfTreatments, numberOfControls);
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setBlockSize(blockSize);
		experimentalDesignInput.setReplicationPercentage(replicationPercentage);
		experimentalDesignInput.setReplicationsCount(replicationCount);
		experimentalDesignInput.setStartingPlotNo(startingPlotNumber);
		final Set<Integer> trialInstancesForDesignGeneration = new HashSet<>(Arrays.asList(1, 2, 3));
		experimentalDesignInput.setTrialInstancesForDesignGeneration(trialInstancesForDesignGeneration);

		final Map<BreedingViewVariableParameter, String> bvVariablesMap = new HashMap<>();
		bvVariablesMap.put(BreedingViewVariableParameter.BLOCK, BLOCK_NO);
		bvVariablesMap.put(BreedingViewVariableParameter.PLOT, PLOT_NO);
		bvVariablesMap.put(BreedingViewVariableParameter.ENTRY, ENTRY_NO);
		when(this.experimentDesignGenerator
			.generate(experimentalDesignInput, bvVariablesMap, studyGermplasmDtoList.size(), null, Collections.singletonMap(
				BreedingViewDesignParameter.NREPEATS, new ArrayList<>()))).thenReturn(mainDesign);

		when(this.measurementVariableGenerator
			.generateFromExperimentalDesignInput(studyId, PROGRAM_UUID, PRepDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				PRepDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES, experimentalDesignInput))
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

	@Test
	public void testCreateReplicationListItemForPRepDesignNoCheckEntries() {

		final int noOfTestEntries = 5;
		final int replicationNumber = 3;
		final int replicationPercentage = 50;
		final float noOfTestEntriesToReplicate = Math.round((float) noOfTestEntries * (replicationPercentage / 100));

		final List<StudyGermplasmDto> importedGermplasmList = StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(5, 0);

		final ExperimentalDesignInput input = new ExperimentalDesignInput();
		input.setReplicationPercentage(replicationPercentage);
		input.setReplicationsCount(replicationNumber);
		final Map<BreedingViewDesignParameter, List<ListItem>> map =
			this.designTypeService
				.createReplicationListItems(importedGermplasmList, input);

		Assert.assertNotNull(map);
		Assert.assertEquals(1, map.size());
		Assert.assertNotNull(map.get(BreedingViewDesignParameter.NREPEATS));

		float countOfReplicatedListItem = 0;
		for (final ListItem listItem : map.get(BreedingViewDesignParameter.NREPEATS)) {
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
		final int replicationPercentage = 50;
		final float noOfTestEntriesToReplicate = Math.round((float) noOfTestEntries * (replicationPercentage / 100));

		final List<StudyGermplasmDto> importedGermplasmList = StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(5, 0);

		// Set the first germplasm as CHECK_ENTRY.
		final StudyGermplasmDto checkImportedGermplasm = importedGermplasmList.get(0);
		checkImportedGermplasm.setCheckType(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());

		final ExperimentalDesignInput input = new ExperimentalDesignInput();
		input.setReplicationPercentage(replicationPercentage);
		input.setReplicationsCount(replicationNumber);
		final Map<BreedingViewDesignParameter, List<ListItem>> map =
			this.designTypeService
				.createReplicationListItems(importedGermplasmList, input);

		Assert.assertNotNull(map);
		Assert.assertEquals(1, map.size());
		Assert.assertNotNull(map.get(BreedingViewDesignParameter.NREPEATS));

		float countOfReplicatedListItem = 0;
		for (final ListItem listItem : map.get(BreedingViewDesignParameter.NREPEATS)) {
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
		final int replicationPercentage = 50;
		final float noOfTestEntriesToReplicate = Math.round((float) noOfTestEntries * (replicationPercentage / 100));

		final List<StudyGermplasmDto> importedGermplasmList = StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(5, 0);

		// Set the first germplasm as CUSTOM ENTRY_TYPE.
		final StudyGermplasmDto checkImportedGermplasm = importedGermplasmList.get(0);
		// Any custom entry type (cagetorical id not in SystemDefineEntryType) is considered as check type.
		final int customEntryTypeCategoricalId = 1000;
		checkImportedGermplasm.setCheckType(customEntryTypeCategoricalId);

		final ExperimentalDesignInput input = new ExperimentalDesignInput();
		input.setReplicationPercentage(replicationPercentage);
		input.setReplicationsCount(replicationNumber);
		final Map<BreedingViewDesignParameter, List<ListItem>> map =
			this.designTypeService
				.createReplicationListItems(importedGermplasmList, input);

		Assert.assertNotNull(map);
		Assert.assertEquals(1, map.size());
		Assert.assertNotNull(map.get(BreedingViewDesignParameter.NREPEATS));

		float countOfReplicatedListItem = 0;
		for (final ListItem listItem : map.get(BreedingViewDesignParameter.NREPEATS)) {
			if (listItem.getValue().equals(String.valueOf(replicationNumber))) {
				countOfReplicatedListItem++;
			}
		}

		Assert.assertEquals(String.valueOf(countOfReplicatedListItem), String.valueOf(noOfTestEntriesToReplicate + noOfCheckEntries));

	}

	private List<StandardVariable> createTestStandardVariables() {
		final List<StandardVariable> standardVariables = new ArrayList<>();
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.ENTRY_NO.getId(), ENTRY_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.BLOCK_NO.getId(), BLOCK_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.PLOT_NO.getId(), PLOT_NO));
		return standardVariables;
	}

}
