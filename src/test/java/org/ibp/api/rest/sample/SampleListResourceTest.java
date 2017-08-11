package org.ibp.api.rest.sample;

import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.service.api.SampleListService;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.security.SecurityServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SampleListResourceTest extends ApiUnitTestBase {

	public static final String P = "P";
	public static final String ADMIN = "admin";
	public static final String DESCRIPTION = "description";
	private static final String YYYY_M_MDD_HH = "yyyyMMddHH";
	private static final String TRIAL_NAME = "trialName#";
	public static final String NOTES = "Notes";
	private static final String CROP_PREFIX = "ABCD";
	public static final String GID = "GID";
	public static final String S = "S";
	public static final String VALUE = "1";

	private SampleListDto dto;
	private User user;

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public SampleListService service() {
			return Mockito.mock(SampleListService.class);
		}
	}

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService service;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		dto = new SampleListDto();
		dto.setDescription(DESCRIPTION);
		dto.setNotes(NOTES);
		dto.setCreatedBy(ADMIN);
		dto.setSelectionVariableId(8263);
		List<Integer> instanceIds = new ArrayList<>();
		instanceIds.add(1);
		dto.setInstanceIds(instanceIds);
		dto.setTakenBy(ADMIN);
		dto.setSamplingDate("2017-08-01");
		dto.setStudyId(new Integer(25025));
		dto.setCropName("maize");

		user = new User();
		user.setName(ADMIN);
		user.setUserid(1);
		user.setPassword("password");
		UsernamePasswordAuthenticationToken loggedInUser =
			new UsernamePasswordAuthenticationToken(this.user.getName(), this.user.getPassword());
		SecurityContextHolder.getContext().setAuthentication(loggedInUser);

		Mockito.when(this.workbenchDataManager.getUserById(this.user.getUserid())).thenReturn(this.user);
		Mockito.when(this.workbenchDataManager.getUserByUsername(this.user.getName())).thenReturn(this.user);
	}

	@Test
	public void createNewSampleList() throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		result.put("id", VALUE);
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/sample/maize/sampleList").build().encode();

		Mockito.when(this.service.createOrUpdateSampleList(Mockito.any(SampleListDTO.class))).thenReturn(Integer.valueOf(VALUE));


		this.mockMvc.perform(MockMvcRequestBuilders.post(uriComponents.toUriString()).contentType(this.contentType)
			.content(this.convertObjectToByte(dto)))
			.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(result.get("id"))));
	}
}
