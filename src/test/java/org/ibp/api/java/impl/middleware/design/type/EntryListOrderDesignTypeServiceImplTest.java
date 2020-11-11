package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.java.impl.middleware.design.generator.MeasurementVariableGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EntryListOrderDesignTypeServiceImplTest {

	public static final String PLOT_NO = "PLOT_NO";
	public static final String ENTRY_NO = "ENTRY_NO";

	@Mock
	private MeasurementVariableGenerator measurementVariableGenerator;

	@InjectMocks
	private final EntryListOrderDesignTypeServiceImpl designTypeService = new EntryListOrderDesignTypeServiceImpl();

	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphanumeric(10);

	@Test
	public void testGenerateDesign() {

		final int studyId = 1;
		final int numberOfTreatments = 5;
		final int numberOfControls = 0;
		final Integer startingPlotNumber = 1;

		final List<StudyEntryDto> studyEntryDtoList =
			StudyEntryTestDataGenerator.createStudyEntryDtoList(numberOfTreatments, numberOfControls);
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		final Set<Integer> trialInstancesForDesignGeneration = new HashSet<>(Arrays.asList(1, 2, 3));
		experimentalDesignInput.setTrialInstancesForDesignGeneration(trialInstancesForDesignGeneration);
		experimentalDesignInput.setStartingPlotNo(startingPlotNumber);

		when(this.measurementVariableGenerator
			.generateFromExperimentalDesignInput(studyId, PROGRAM_UUID, EntryListOrderDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				EntryListOrderDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES, experimentalDesignInput))
			.thenReturn(this.createMeasurementVariables());

		final List<ObservationUnitRow> result =
			this.designTypeService.generateDesign(studyId, experimentalDesignInput, PROGRAM_UUID, studyEntryDtoList);

		assertEquals(numberOfTreatments * trialInstancesForDesignGeneration.size(), result.size());
		final Map<Integer, List<ObservationUnitRow>> instancesRowMap = new HashMap<>();
		Integer currentTrialInstance = 1;
		int index = 1;
		int germplasmId = 100;
		for (final ObservationUnitRow row : result) {
			final Integer trialInstance = row.getTrialInstance();
			if (!currentTrialInstance.equals(trialInstance)) {
				currentTrialInstance = trialInstance;
				index = 1;
				germplasmId = 100;
			}
			instancesRowMap.putIfAbsent(trialInstance, new ArrayList<>());
			instancesRowMap.get(trialInstance).add(row);

			// Verify row values
			assertEquals(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()),
				row.getVariables().get(String.valueOf(TermId.ENTRY_TYPE.getId())).getValue());
			assertEquals(new Integer(index), row.getEntryNumber());
			assertEquals(row.getEntryNumber().toString(), row.getVariables().get(String.valueOf(TermId.ENTRY_NO.getId())).getValue());
			assertEquals(row.getEntryNumber().toString(), row.getVariables().get(String.valueOf(TermId.PLOT_NO.getId())).getValue());
			assertEquals(
				Integer.toString(germplasmId),
				row.getVariables().get(String.valueOf(TermId.GID.getId())).getValue());
			index++;
			germplasmId++;

		}
		assertEquals(numberOfTreatments, instancesRowMap.get(1).size());
		assertEquals(numberOfTreatments, instancesRowMap.get(2).size());
		assertEquals(numberOfTreatments, instancesRowMap.get(3).size());

	}

	@Test
	public void testLoadChecksAndTestEntries() {
		final int numberOfTreatments = 5;
		final int numberOfControls = 3;
		final List<StudyEntryDto> studyEntryDtoList =
			StudyEntryTestDataGenerator.createStudyEntryDtoList(numberOfTreatments, numberOfControls);
		//Reverse the study entries list by entry number
		studyEntryDtoList.sort(Comparator.comparing(StudyEntryDto::getEntryNumber).reversed());
		final List<StudyEntryDto> checkList = new ArrayList<>();
		final List<StudyEntryDto> testEntryList = new ArrayList<>();

		this.designTypeService.loadChecksAndTestEntries(studyEntryDtoList, checkList, testEntryList);

		Assert.assertEquals(numberOfControls, checkList.size());
		for(int i=1; i<=numberOfControls; i++) {
			Assert.assertEquals(new Integer(i), checkList.get(i-1).getEntryNumber());
		}

		Assert.assertEquals(numberOfTreatments, testEntryList.size());
		for(int i=1; i<=numberOfTreatments; i++) {
			final Integer entryNumber = i + numberOfControls;
			Assert.assertEquals(entryNumber, testEntryList.get(i-1).getEntryNumber());
		}
	}

	@Test
	public void testGenerateDesignWithCheckPlan() {

		final int studyId = 1;
		final int numberOfTreatments = 5;
		final int numberOfControls = 1;
		final Integer checkStartingPosition = 1;
		final Integer checkSpacing = 2;
		final Integer checkInsertionManner = InsertionMannerItem.INSERT_EACH_IN_TURN.getId();
		final Integer startingPlotNumber = 1;

		final List<StudyEntryDto> studyEntryDtoList =
			StudyEntryTestDataGenerator.createStudyEntryDtoList(numberOfTreatments, numberOfControls);
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setCheckStartingPosition(checkStartingPosition);
		experimentalDesignInput.setCheckSpacing(checkSpacing);
		experimentalDesignInput.setCheckInsertionManner(checkInsertionManner);
		final Set<Integer> trialInstancesForDesignGeneration = new HashSet<>(Arrays.asList(1, 2, 3));
		experimentalDesignInput.setTrialInstancesForDesignGeneration(trialInstancesForDesignGeneration);
		experimentalDesignInput.setStartingPlotNo(startingPlotNumber);

		when(this.measurementVariableGenerator
			.generateFromExperimentalDesignInput(studyId, PROGRAM_UUID, EntryListOrderDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				EntryListOrderDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES_WITH_CHECK_PLAN, experimentalDesignInput))
			.thenReturn(this.createMeasurementVariables());

		final List<ObservationUnitRow> result =
			this.designTypeService.generateDesign(studyId, experimentalDesignInput, PROGRAM_UUID, studyEntryDtoList);

		assertEquals(8 * trialInstancesForDesignGeneration.size(), result.size());
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

	private List<MeasurementVariable> createMeasurementVariables() {
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
