package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
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
	private WorkbenchDataManager workbenchDataManager;

	private ProgramValidator programValidator;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		programValidator = new ProgramValidator();
		programValidator.setWorkbenchDataManager(this.workbenchDataManager);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void testForEmptyProgramId(){

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");
		String programId = "";

		this.programValidator.validate(programId, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	@Test
	public void testForInvalidProgramId() throws MiddlewareQueryException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");
		String programId = "uuid";

		Mockito.doReturn(null).when(this.workbenchDataManager).getProjectByUuid(programId);

		this.programValidator.validate(programId, bindingResult);
		Assert.assertTrue(bindingResult.hasErrors());
	}

	@Test
	public void testForValidProgramId() throws MiddlewareQueryException {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Program");
		String programId = "uuid";

		Project project = new Project();
		project.setUniqueID(programId);
		project.setProjectId(1L);
		project.setProjectName("Crop_Program");

		Mockito.doReturn(project).when(this.workbenchDataManager).getProjectByUuid(programId);

		this.programValidator.validate(programId, bindingResult);
		Assert.assertFalse(bindingResult.hasErrors());
	}
}
