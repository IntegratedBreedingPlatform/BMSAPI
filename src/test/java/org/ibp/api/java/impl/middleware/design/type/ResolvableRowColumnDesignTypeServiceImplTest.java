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
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignTypeValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResolvableRowColumnDesignTypeServiceImplTest {

	public static final String ENTRY_NO = "ENTRY_NO";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String BLOCK_NO = "BLOCK_NO";
	public static final String REP_NO = "REP_NO";
	public static final String ROW = "ROW";
	public static final String COL = "COL";

	@Mock
	public ExperimentDesignTypeValidator experimentDesignTypeValidator;

	@Mock
	public ExperimentDesignGenerator experimentDesignGenerator;

	@Mock
	public OntologyDataManager ontologyDataManager;

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
		final Integer numberOfTrials = 3;

		final List<StudyGermplasmDto> studyGermplasmDtoList =
			StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(numberOfTreatments, numberOfControls);
		final ExperimentDesignInput experimentDesignInput = new ExperimentDesignInput();
		experimentDesignInput.setReplicationsCount(replicationsCount);
		experimentDesignInput.setRowsPerReplications(rowPerReplications);
		experimentDesignInput.setColsPerReplications(colPerReplications);
		experimentDesignInput.setStartingPlotNo(startingPlotNumber);
		experimentDesignInput.setNoOfEnvironments(numberOfTrials);
		experimentDesignInput.setUseLatenized(false);

		when(this.experimentDesignGenerator
			.createResolvableRowColDesign(studyGermplasmDtoList.size(), replicationsCount, rowPerReplications, colPerReplications,
				ENTRY_NO, REP_NO, ROW, COL, PLOT_NO, startingPlotNumber, experimentDesignInput.getNrlatin(),
				experimentDesignInput.getNclatin(), experimentDesignInput.getReplatinGroups(), "", experimentDesignInput.getUseLatenized()))
			.thenReturn(mainDesign);
		when(this.experimentDesignGenerator
			.constructMeasurementVariables(studyId, PROGRAM_UUID, ResolvableRowColumnDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				ResolvableRowColumnDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES, experimentDesignInput))
			.thenReturn(measurementVariables);
		when(this.experimentDesignGenerator
			.generateExperimentDesignMeasurements(eq(numberOfTrials), refEq(measurementVariables),
				refEq(studyGermplasmDtoList), refEq(mainDesign),
				eq(ENTRY_NO),
				isNull(), any(Map.class))).thenReturn(observationUnitRowList);

		final List<ObservationUnitRow> result =
			this.designTypeService.generateDesign(studyId, experimentDesignInput, PROGRAM_UUID, studyGermplasmDtoList);

		assertSame(result, observationUnitRowList);
		verify(this.experimentDesignTypeValidator).validateResolvableRowColumnDesign(experimentDesignInput, studyGermplasmDtoList);

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
		final Integer numberOfTrials = 3;

		final List<StudyGermplasmDto> studyGermplasmDtoList =
			StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(numberOfTreatments, numberOfControls);
		final ExperimentDesignInput experimentDesignInput = new ExperimentDesignInput();
		experimentDesignInput.setReplicationsCount(replicationsCount);
		experimentDesignInput.setRowsPerReplications(rowPerReplications);
		experimentDesignInput.setColsPerReplications(colPerReplications);
		experimentDesignInput.setStartingPlotNo(startingPlotNumber);
		experimentDesignInput.setNoOfEnvironments(numberOfTrials);

		// Set value 1 for Column Arrangement
		experimentDesignInput.setReplicationsArrangement(1);
		experimentDesignInput.setUseLatenized(true);

		when(this.experimentDesignGenerator
			.createResolvableRowColDesign(studyGermplasmDtoList.size(), replicationsCount, rowPerReplications, colPerReplications,
				ENTRY_NO, REP_NO, ROW, COL, PLOT_NO, startingPlotNumber, experimentDesignInput.getNrlatin(),
				experimentDesignInput.getNclatin(), String.valueOf(replicationsCount), "", experimentDesignInput.getUseLatenized()))
			.thenReturn(mainDesign);
		when(this.experimentDesignGenerator
			.constructMeasurementVariables(studyId, PROGRAM_UUID, ResolvableRowColumnDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				ResolvableRowColumnDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES_LATINIZED, experimentDesignInput))
			.thenReturn(measurementVariables);
		when(this.experimentDesignGenerator
			.generateExperimentDesignMeasurements(eq(numberOfTrials), refEq(measurementVariables),
				refEq(studyGermplasmDtoList), refEq(mainDesign),
				eq(ENTRY_NO),
				isNull(), any(Map.class))).thenReturn(observationUnitRowList);

		final List<ObservationUnitRow> result =
			this.designTypeService.generateDesign(studyId, experimentDesignInput, PROGRAM_UUID, studyGermplasmDtoList);

		assertSame(result, observationUnitRowList);
		verify(this.experimentDesignTypeValidator).validateResolvableRowColumnDesign(experimentDesignInput, studyGermplasmDtoList);

	}

	List<StandardVariable> createTestStandardVariables() {
		final List<StandardVariable> standardVariables = new ArrayList<>();
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.ENTRY_NO.getId(), ENTRY_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.PLOT_NO.getId(), PLOT_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.REP_NO.getId(), REP_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.ROW.getId(), ROW));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.COL.getId(), COL));
		return standardVariables;
	}

}
