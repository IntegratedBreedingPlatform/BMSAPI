package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.design.ListItem;
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
public class PRepDesignTypeServiceImplTest {

	public static final String ENTRY_NO = "ENTRY_NO";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String BLOCK_NO = "BLOCK_NO";
	@Mock
	public ExperimentDesignTypeValidator experimentDesignTypeValidator;

	@Mock
	public ExperimentDesignGenerator experimentDesignGenerator;

	@Mock
	public OntologyDataManager ontologyDataManager;

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
		final List<ListItem> replicationListItems = new ArrayList<>();

		final int studyId = 1;
		final int numberOfTreatments = 10;
		final int numberOfControls = 5;
		final String blockSize = "5";
		final String replicationCount = "3";
		final int replicationPercentage = 50;
		final String numberOfTrials = "1";
		final String startingPlotNumber = "1";

		final List<StudyGermplasmDto> studyGermplasmDtoList =
			StudyGermplasmTestDataGenerator.createStudyGermplasmDtoList(numberOfTreatments, numberOfControls);
		final ExperimentDesignInput experimentDesignInput = new ExperimentDesignInput();
		experimentDesignInput.setBlockSize(blockSize);
		experimentDesignInput.setReplicationPercentage(replicationPercentage);
		experimentDesignInput.setReplicationsCount(replicationCount);
		experimentDesignInput.setNoOfEnvironments(numberOfTrials);

		when(this.experimentDesignGenerator
			.createReplicationListItemForPRepDesign(studyGermplasmDtoList, replicationPercentage, Integer.parseInt(replicationCount)))
			.thenReturn(replicationListItems);
		when(this.experimentDesignGenerator
			.createPRepDesign(Integer.parseInt(blockSize), studyGermplasmDtoList.size(), replicationListItems,
				ENTRY_NO,
				BLOCK_NO, PLOT_NO, Integer.parseInt(startingPlotNumber))).thenReturn(mainDesign);
		when(this.experimentDesignGenerator
			.constructMeasurementVariables(studyId, PROGRAM_UUID, PRepDesignTypeServiceImpl.DESIGN_FACTOR_VARIABLES,
				PRepDesignTypeServiceImpl.EXPERIMENT_DESIGN_VARIABLES, experimentDesignInput))
			.thenReturn(measurementVariables);
		when(this.experimentDesignGenerator
			.generateExperimentDesignMeasurements(eq(Integer.parseInt(numberOfTrials)), refEq(measurementVariables),
				refEq(studyGermplasmDtoList), refEq(mainDesign),
				eq(ENTRY_NO),
				isNull(), any(Map.class))).thenReturn(observationUnitRowList);

		final List<ObservationUnitRow> result =
			this.designTypeService.generateDesign(studyId, experimentDesignInput, PROGRAM_UUID, studyGermplasmDtoList);

		assertSame(result, observationUnitRowList);
		verify(this.experimentDesignTypeValidator).validatePrepDesign(experimentDesignInput, studyGermplasmDtoList);

	}

	List<StandardVariable> createTestStandardVariables() {
		final List<StandardVariable> standardVariables = new ArrayList<>();
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.ENTRY_NO.getId(), ENTRY_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.BLOCK_NO.getId(), BLOCK_NO));
		standardVariables.add(StandardVariableTestDataInitializer.createStandardVariable(TermId.PLOT_NO.getId(), PLOT_NO));
		return standardVariables;
	}

}
