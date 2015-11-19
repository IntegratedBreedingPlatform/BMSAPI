
package org.ibp.api.rest.program;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.program.ProgramServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.google.common.collect.Lists;

public class ProgramResourceTest extends ApiUnitTestBase {

	private UsernamePasswordAuthenticationToken loggedInUser;
	private User me;
	private User myBreedingBuddy;

	@Before
	public void beforeEachTest() {

		this.me = new User();
		this.me.setName("Mr. Breeder");
		this.me.setUserid(1);
		this.me.setPassword("password");

		this.myBreedingBuddy = new User();
		this.myBreedingBuddy.setName("My Breeding Buddy");
		this.myBreedingBuddy.setUserid(2);
		this.myBreedingBuddy.setPassword("password");

		this.loggedInUser = new UsernamePasswordAuthenticationToken(this.me.getName(), this.me.getPassword());
		SecurityContextHolder.getContext().setAuthentication(this.loggedInUser);

		Mockito.when(this.workbenchDataManager.getUserById(this.me.getUserid())).thenReturn(this.me);
		Mockito.when(this.workbenchDataManager.getUserById(this.myBreedingBuddy.getUserid())).thenReturn(this.myBreedingBuddy);

		Mockito.when(this.workbenchDataManager.getUserByUsername(this.me.getName())).thenReturn(this.me);
		Mockito.when(this.workbenchDataManager.getUserByUsername(this.myBreedingBuddy.getName())).thenReturn(this.myBreedingBuddy);
	}

	@After
	public void afterEachTest() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	@Test
	public void listAllMethods() throws Exception {

		CropType cropType = new CropType();
		cropType.setCropName("MAIZE");

		List<Project> programList = new ArrayList<>();
		Project program1 = new Project();
		program1.setProjectId(1L);
		program1.setProjectName("Program I Created");
		program1.setCropType(cropType);
		program1.setUniqueID("fb0783d2-dc82-4db6-a36e-7554d3740092");
		program1.setUserId(this.me.getUserid());
		final String program1Date = "2015-11-11";
		program1.setStartDate(ProgramServiceImpl.DATE_FORMAT.parse(program1Date));

		programList.add(program1);

		Project program2 = new Project();
		program2.setProjectId(2L);
		program2.setProjectName("Program I am member of");
		program2.setCropType(cropType);
		program2.setUniqueID("57b8f271-56db-448e-ad8d-528ac4d80f04");
		program2.setUserId(this.myBreedingBuddy.getUserid());
		final String program2Date = "2015-12-12";
		program2.setStartDate(ProgramServiceImpl.DATE_FORMAT.parse(program2Date));

		programList.add(program2);

		Mockito.doReturn(programList).when(this.workbenchDataManager).getProjectsByUser(Mockito.eq(this.me));

		Mockito.when(this.workbenchDataManager.getUsersByProjectId(program1.getProjectId())).thenReturn(Lists.newArrayList(this.me));
		Mockito.when(this.workbenchDataManager.getUsersByProjectId(program2.getProjectId())).thenReturn(
				Lists.newArrayList(this.me, this.myBreedingBuddy));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/program/list").contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print()).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", Matchers.is(String.valueOf(program1.getProjectId()))))
				.andExpect(jsonPath("$[0].uniqueID", Matchers.is(program1.getUniqueID())))
				.andExpect(jsonPath("$[0].name", Matchers.is(program1.getProjectName())))
				.andExpect(jsonPath("$[0].members", Matchers.contains(this.me.getName())))
				.andExpect(jsonPath("$[0].crop", Matchers.is(program1.getCropType().getCropName())))
				.andExpect(jsonPath("$[0].startDate", Matchers.is(program1Date)))
				.andExpect(jsonPath("$[0].createdBy", Matchers.is(this.me.getName())))

				.andExpect(jsonPath("$[1].id", Matchers.is(String.valueOf(program2.getProjectId()))))
				.andExpect(jsonPath("$[1].uniqueID", Matchers.is(program2.getUniqueID())))
				.andExpect(jsonPath("$[1].name", Matchers.is(program2.getProjectName())))
				.andExpect(jsonPath("$[1].members", Matchers.contains(this.me.getName(), this.myBreedingBuddy.getName())))
				.andExpect(jsonPath("$[1].crop", Matchers.is(program2.getCropType().getCropName())))
				.andExpect(jsonPath("$[1].startDate", Matchers.is(program2Date)))
				.andExpect(jsonPath("$[1].createdBy", Matchers.is(this.myBreedingBuddy.getName())));
	}
}
