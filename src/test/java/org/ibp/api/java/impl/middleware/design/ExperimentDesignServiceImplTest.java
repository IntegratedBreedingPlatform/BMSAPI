package org.ibp.api.java.impl.middleware.design;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.java.design.DesignLicenseService;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.impl.middleware.design.type.ExperimentDesignTypeServiceFactory;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignValidator;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class ExperimentDesignServiceImplTest {

	private static final String CROP = "maize";
	private static final int STUDY_ID = 123;
	private static final String PROGRAM_UUID = RandomStringUtils.random(20);

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private ExperimentDesignValidator experimentDesignValidator;

	@Mock
	private StudyService studyService;

	@Mock
	private DesignLicenseService designLicenseService;

	@Mock
	private ExperimentDesignTypeServiceFactory experimentDesignTypeServiceFactory;

	@Mock
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Mock
	private org.generationcp.middleware.service.api.study.generation.ExperimentDesignService middlewareExperimentDesignService;

	@Mock
	private ExperimentDesignTypeService designTypeService;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@InjectMocks
	private ExperimentDesignServiceImpl experimentDesignService;

	private final ExperimentDesignInput designInput = new ExperimentDesignInput();
	private final CropType cropType = new CropType();
	private final List<StudyGermplasmDto> studyList = new ArrayList<>();
	private final List<MeasurementVariable> variables = new ArrayList<>();
	private final List<ObservationUnitRow> rows = new ArrayList<>();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(this.designTypeService).when(this.experimentDesignTypeServiceFactory).lookup(ArgumentMatchers.anyInt());
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
			Assert.assertEquals(ExperimentDesignServiceImpl.EXPERIMENT_DESIGN_LICENSE_EXPIRED, e.getError().getCodes()[0]);
		}
		Mockito.verifyZeroInteractions(this.studyValidator);
		Mockito.verifyZeroInteractions(this.studyService);
		Mockito.verifyZeroInteractions(this.middlewareStudyService);
		Mockito.verifyZeroInteractions(this.workbenchDataManager);
		Mockito.verifyZeroInteractions(this.middlewareExperimentDesignService);
	}

	@Test
	public void testDeleteDesign() {
		this.experimentDesignService.deleteDesign(STUDY_ID);
		Mockito.verify(this.studyValidator).validate(STUDY_ID, true);
		Mockito.verify(this.experimentDesignValidator).validateExperimentDesignExistence(STUDY_ID, true);
		Mockito.verify(this.middlewareExperimentDesignService).deleteStudyExperimentDesign(STUDY_ID);
	}

	@Test
	public void testGenerateDesign() {
		Mockito.doReturn(this.cropType).when(this.workbenchDataManager).getCropTypeByName(CROP);
		Mockito.doReturn(PROGRAM_UUID).when(this.studyService).getProgramUUID(STUDY_ID);
		Mockito.doReturn(this.studyList).when(this.middlewareStudyService).getStudyGermplasmList(STUDY_ID);
		Mockito.doReturn(this.rows).when(this.designTypeService).generateDesign(STUDY_ID, this.designInput, PROGRAM_UUID, this.studyList);
		Mockito.doReturn(this.variables).when(this.designTypeService).getMeasurementVariables(STUDY_ID, this.designInput, PROGRAM_UUID);

		// Method to test
		this.experimentDesignService.generateAndSaveDesign(CROP, STUDY_ID, this.designInput);
		Mockito.verifyZeroInteractions(this.designLicenseService);
		Mockito.verify(this.studyValidator).validate(STUDY_ID, true);
		Mockito.verify(this.designTypeService).generateDesign(STUDY_ID, this.designInput, PROGRAM_UUID, this.studyList);
		Mockito.verify(this.designTypeService).getMeasurementVariables(STUDY_ID, this.designInput, PROGRAM_UUID);
		Mockito.verify(this.middlewareExperimentDesignService).deleteStudyExperimentDesign(STUDY_ID);
		Mockito.verify(this.middlewareExperimentDesignService)
			.saveExperimentDesign(ArgumentMatchers.eq(this.cropType), ArgumentMatchers.eq(STUDY_ID), ArgumentMatchers.eq(this.variables),
				ArgumentMatchers.anyList());
	}

}
