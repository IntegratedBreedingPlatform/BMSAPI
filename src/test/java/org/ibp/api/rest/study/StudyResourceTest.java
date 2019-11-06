
package org.ibp.api.rest.study;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudySummary;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Random;

public class StudyResourceTest extends ApiUnitTestBase {

	private static final int USER_ID = 1;

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService studyServiceMW;

	@Autowired
	private SecurityService securityService;

	@Before
	public void init() {
		Mockito.reset(this.securityService);
		Mockito.doReturn(true).when(this.securityService).isAccessible(ArgumentMatchers.any(StudySummary.class), ArgumentMatchers.anyString());
		final WorkbenchUser user = new WorkbenchUser();
		user.setUserid(USER_ID);
		Mockito.doReturn(user).when(this.securityService).getCurrentlyLoggedInUser();
	}

	@Test
	public void testListStudyInstances() throws Exception {

		final StudyInstance studyInstance = new StudyInstance(1, 1, "Gujarat, India", "GUJ", 1, "", true);
		final Random random = new Random();
		final Boolean hasExptDesign = random.nextBoolean();
		studyInstance.setHasExperimentalDesign(hasExptDesign);
		final Boolean hasMeasurements = random.nextBoolean();
		studyInstance.setHasMeasurements(hasMeasurements);
		final Boolean canBeRegenerated = random.nextBoolean();
		studyInstance.setDesignRegenerationAllowed(canBeRegenerated);
		Mockito.when(this.studyServiceMW.getStudyInstances(ArgumentMatchers.anyInt()))
				.thenReturn(Lists.newArrayList(studyInstance));

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/study/{cropname}/{studyId}/instances", "maize", "1")
				.contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].instanceDbId", Matchers.is(studyInstance.getInstanceDbId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].locationName", Matchers.is(studyInstance.getLocationName())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].locationAbbreviation", Matchers.is(studyInstance.getLocationAbbreviation())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].instanceNumber", Matchers.is(studyInstance.getInstanceNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].hasFieldmap", Matchers.is(studyInstance.isHasFieldmap())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].hasMeasurements", Matchers.is(studyInstance.isHasMeasurements())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].hasExperimentalDesign", Matchers.is(studyInstance.isHasExperimentalDesign())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].designRegenerationAllowed", Matchers.is(studyInstance.isDesignRegenerationAllowed())));
	}
}
