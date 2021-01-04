package org.ibp.api.java.impl.middleware.design;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.impl.inventory.PlantingServiceImpl;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.java.design.DesignLicenseService;
import org.ibp.api.java.design.type.ExperimentalDesignTypeService;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.impl.middleware.design.type.ExperimentalDesignTypeServiceFactory;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentalDesignTypeValidator;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentalDesignValidator;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ExperimentalDesignServiceImplTest {

	private static final String CROP = "maize";
	private static final int STUDY_ID = 123;
	private static final String PROGRAM_UUID = RandomStringUtils.random(20);

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private ExperimentalDesignValidator experimentalDesignValidator;

	@Mock
	private InstanceValidator instanceValidator;

	@Mock
	private ExperimentalDesignTypeValidator experimentalDesignTypeValidator;

	@Mock
	private StudyService studyService;

	@Mock
	private PlantingServiceImpl plantingService;

	@Mock
	private DesignLicenseService designLicenseService;

	@Mock
	private ExperimentalDesignTypeServiceFactory experimentalDesignTypeServiceFactory;

	@Mock
	private StudyEntryService middlewareStudyEntryService;

	@Mock
	private org.generationcp.middleware.service.api.study.generation.ExperimentDesignService middlewareExperimentDesignService;

	@Mock
	private ExperimentalDesignTypeService designTypeService;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@InjectMocks
	private ExperimentalDesignServiceImpl experimentDesignService;

	private final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
	private final CropType cropType = new CropType();
	private final List<StudyEntryDto> studyList = new ArrayList<>();
	private final List<MeasurementVariable> variables = new ArrayList<>();
	private final List<ObservationUnitRow> rows = new ArrayList<>();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(this.designTypeService).when(this.experimentalDesignTypeServiceFactory).lookup(ArgumentMatchers.anyInt());
		Mockito.doReturn(false).when(this.designTypeService).requiresLicenseCheck();

		this.designInput.setDesignType(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId());
	}

	@Test
	public void testGenerateDesignExpiredLicense() {
		Mockito.doReturn(true).when(this.designTypeService).requiresLicenseCheck();
		Mockito.doReturn(true).when(this.designLicenseService).isExpired();

		try {
			this.experimentDesignService.generateAndSaveDesign(CROP, STUDY_ID, this.designInput);
			Assert.fail("Expected Forbidden exception to be thrown but was not.");
		} catch (final ForbiddenException e) {
			Assert.assertNotNull(e.getError().getCodes());
			Assert.assertEquals(ExperimentalDesignServiceImpl.EXPERIMENT_DESIGN_LICENSE_EXPIRED, e.getError().getCodes()[0]);
		}
		Mockito.verify(this.studyValidator).validate(STUDY_ID, true);
		Mockito.verify(this.experimentalDesignValidator).validateStudyExperimentalDesign(STUDY_ID, this.designInput.getDesignType());
		Mockito.verify(this.instanceValidator).validateInstanceNumbers(STUDY_ID, this.designInput.getTrialInstancesForDesignGeneration());
		Mockito.verifyZeroInteractions(this.studyService);
		Mockito.verifyZeroInteractions(this.middlewareStudyEntryService);
		Mockito.verifyZeroInteractions(this.workbenchDataManager);
		Mockito.verifyZeroInteractions(this.experimentalDesignTypeValidator);
		Mockito.verifyZeroInteractions(this.middlewareExperimentDesignService);
	}

	@Test
	public void testDeleteDesign() {
		Mockito.when(plantingService.getPlantingTransactionsByStudyId(STUDY_ID, TransactionStatus.PENDING)).thenReturn(new ArrayList<>());
		Mockito.when(plantingService.getPlantingTransactionsByStudyId(STUDY_ID, TransactionStatus.CONFIRMED)).thenReturn(new ArrayList<>());
		this.experimentDesignService.deleteDesign(STUDY_ID);
		Mockito.verify(this.studyValidator).validate(STUDY_ID, true, true);
		Mockito.verify(this.experimentalDesignValidator).validateExperimentalDesignExistence(STUDY_ID, true);
		Mockito.verify(this.middlewareExperimentDesignService).deleteStudyExperimentDesign(STUDY_ID);
	}

	@Test
	public void testGenerateDesign() {
		Mockito.doReturn(this.cropType).when(this.workbenchDataManager).getCropTypeByName(CROP);
		Mockito.doReturn(PROGRAM_UUID).when(this.studyService).getProgramUUID(STUDY_ID);
		Mockito.doReturn(this.studyList).when(this.middlewareStudyEntryService).getStudyEntries(STUDY_ID);
		Mockito.doReturn(this.rows).when(this.designTypeService).generateDesign(STUDY_ID, this.designInput, PROGRAM_UUID, this.studyList);
		Mockito.doReturn(this.variables).when(this.designTypeService).getMeasurementVariables(STUDY_ID, this.designInput, PROGRAM_UUID);

		// Method to test
		this.experimentDesignService.generateAndSaveDesign(CROP, STUDY_ID, this.designInput);
		Mockito.verifyZeroInteractions(this.designLicenseService);
		Mockito.verify(this.studyValidator).validate(STUDY_ID, true);
		Mockito.verify(this.experimentalDesignValidator).validateStudyExperimentalDesign(STUDY_ID, this.designInput.getDesignType());
		Mockito.verify(this.instanceValidator).validateInstanceNumbers(STUDY_ID, this.designInput.getTrialInstancesForDesignGeneration());
		Mockito.verify(this.experimentalDesignTypeValidator).validate(this.designInput, this.studyList);
		Mockito.verify(this.designTypeService).generateDesign(STUDY_ID, this.designInput, PROGRAM_UUID, this.studyList);
		Mockito.verify(this.designTypeService).getMeasurementVariables(STUDY_ID, this.designInput, PROGRAM_UUID);
		// FIXME perform assertions on the observation unit rows map
		Mockito.verify(this.middlewareExperimentDesignService)
			.saveExperimentDesign(ArgumentMatchers.eq(this.cropType), ArgumentMatchers.eq(STUDY_ID), ArgumentMatchers.eq(this.variables),
				ArgumentMatchers.anyMap());
	}

	@Test
	public void testGetExperimentalDesignTypes() {
		final List<ExperimentDesignType> types = Arrays.asList(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK,
			ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK,
			ExperimentDesignType.ROW_COL,
			ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK,
			ExperimentDesignType.CUSTOM_IMPORT,
			ExperimentDesignType.ENTRY_LIST_ORDER,
			ExperimentDesignType.P_REP);
		Assert.assertEquals(types, this.experimentDesignService.getExperimentalDesignTypes());
	}

	@Test
	public void testGetStudyExperimentalDesignTypeTermId() {
		Mockito.doReturn(Optional.empty()).when(this.middlewareExperimentDesignService).getStudyExperimentDesignTypeTermId(STUDY_ID);
		Assert.assertFalse(this.experimentDesignService.getStudyExperimentalDesignTypeTermId(STUDY_ID).isPresent());

		final Integer termId = ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getTermId();
		Mockito.doReturn(Optional.of(termId)).when(this.middlewareExperimentDesignService).getStudyExperimentDesignTypeTermId(STUDY_ID);
		Assert.assertEquals(termId, this.experimentDesignService.getStudyExperimentalDesignTypeTermId(STUDY_ID).get());

	}

}
