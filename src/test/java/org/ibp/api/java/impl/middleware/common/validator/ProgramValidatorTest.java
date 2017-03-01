
package org.ibp.api.java.impl.middleware.common.validator;

import java.util.HashMap;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.ibp.api.domain.program.ProgramSummary;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

public class ProgramValidatorTest {

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	private ProgramValidator programValidator;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.programValidator = new ProgramValidator();
		this.programValidator.setWorkbenchDataManager(this.workbenchDataManager);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void testForEmptyProgramId() {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");

		ProgramSummary program = new ProgramSummary();
		program.setCrop("maize");
		program.setUniqueID("");

		this.programValidator.validate(program, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	@Test
	public void testForInvalidProgramId() throws MiddlewareQueryException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");
		String programId = "uuid";
		String cropname = "maize";

		ProgramSummary program = new ProgramSummary();
		program.setCrop(cropname);
		program.setUniqueID(programId);

		Mockito.doReturn(null).when(this.workbenchDataManager).getProjectByUuid(programId, cropname);

		this.programValidator.validate(program, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	@Test
	public void testForCropNameOfProgramNotMatch() throws MiddlewareQueryException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");

		String programId = "uuid";
		String cropname = "maize";

		ProgramSummary program = new ProgramSummary();
		program.setCrop(cropname);
		program.setUniqueID(programId);

		Project project = new Project();
		project.setUniqueID(programId);
		project.setProjectId(1L);
		project.setProjectName("Crop_Program");
		project.setCropType(new CropType("rice"));

		Mockito.doReturn(project).when(this.workbenchDataManager).getProjectByUuid(programId, cropname);

		this.programValidator.validate(program, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	@Test
	public void testForValidProgramId() throws MiddlewareQueryException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");

		String programId = "uuid";
		String cropname = "maize";

		ProgramSummary program = new ProgramSummary();
		program.setCrop(cropname);
		program.setUniqueID(programId);

		Project project = new Project();
		project.setUniqueID(programId);
		project.setProjectId(1L);
		project.setProjectName("Crop_Program");
		project.setCropType(new CropType(cropname));

		Mockito.doReturn(project).when(this.workbenchDataManager).getProjectByUuid(programId, cropname);

		this.programValidator.validate(program, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
