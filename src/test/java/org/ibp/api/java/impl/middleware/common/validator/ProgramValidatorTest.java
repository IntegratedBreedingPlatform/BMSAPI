
package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

public class ProgramValidatorTest {

	@Mock
	private ProgramService programService;

	private ProgramValidator programValidator;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.programValidator = new ProgramValidator();
		this.programValidator.setProgramService(this.programService);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void testForEmptyProgramId() {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");

		ProgramDTO program = new ProgramDTO();
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

		ProgramDTO program = new ProgramDTO();
		program.setCrop(cropname);
		program.setUniqueID(programId);

		Mockito.doReturn(null).when(this.programService).getProjectByUuidAndCrop(programId, cropname);

		this.programValidator.validate(program, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	@Test
	public void testForCropNameOfProgramNotMatch() throws MiddlewareQueryException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");

		String programId = "uuid";
		String cropname = "maize";

		ProgramDTO program = new ProgramDTO();
		program.setCrop(cropname);
		program.setUniqueID(programId);

		Project project = new Project();
		project.setUniqueID(programId);
		project.setProjectId(1L);
		project.setProjectName("Crop_Program");
		project.setCropType(new CropType("rice"));

		Mockito.doReturn(project).when(this.programService).getProjectByUuidAndCrop(programId, cropname);

		this.programValidator.validate(program, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	@Test
	public void testForValidProgramId() throws MiddlewareQueryException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");

		String programId = "uuid";
		String cropname = "maize";

		ProgramDTO program = new ProgramDTO();
		program.setCrop(cropname);
		program.setUniqueID(programId);

		Project project = new Project();
		project.setUniqueID(programId);
		project.setProjectId(1L);
		project.setProjectName("Crop_Program");
		project.setCropType(new CropType(cropname));

		Mockito.doReturn(project).when(this.programService).getProjectByUuidAndCrop(programId, cropname);

		this.programValidator.validate(program, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
