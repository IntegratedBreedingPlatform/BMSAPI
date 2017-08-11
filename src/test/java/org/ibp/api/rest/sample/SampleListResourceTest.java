package org.ibp.api.rest.sample;

import org.generationcp.middleware.pojos.User;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.security.SecurityServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public static final Integer VALUE = new Integer(1);

	private SampleListDto dto;
	private User user;

	private SampleListServiceImpl sampleListService;
	private SecurityServiceImpl securityService;
	private org.generationcp.middleware.service.impl.study.SampleListServiceImpl service;

	@Before
	public void beforeEachTest() {
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

		sampleListService = Mockito.mock(SampleListServiceImpl.class);
		service = Mockito.mock(org.generationcp.middleware.service.impl.study.SampleListServiceImpl.class);
		securityService = Mockito.mock(SecurityServiceImpl.class);

		sampleListService.setSecurityService(securityService);
		sampleListService.setService(service);
	}

	@Test
	public void createNewSampleList() throws Exception {
		HashMap<String, Object> result = new HashMap<>();
		result.put("id", VALUE);
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/sample/maize/sampleList").build().encode();

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		Mockito.when(this.sampleListService.createSampleList(Mockito.any(SampleListDto.class))).thenReturn(result);


		this.mockMvc.perform(MockMvcRequestBuilders.post(uriComponents.toUriString()).contentType(this.contentType)
			.content(this.convertObjectToByte(dto)))
			.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(result.get("id"))));
	}
}
