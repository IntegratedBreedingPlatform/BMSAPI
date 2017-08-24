package org.ibp.api.rest.sample;

import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.enumeration.SampleListType;
import org.generationcp.middleware.pojos.SampleList;
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
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ActiveProfiles("security-mocked")
public class SampleListResourceTest extends ApiUnitTestBase {

	private static final String ADMIN = "admin";
	private static final String DESCRIPTION = "description";
	private static final String NOTES = "Notes";
	private static final String VALUE = "1";

	private SampleListDto dto;
	private User user;
	private String folderName;
	private Integer parentId;
	private Integer folderId;


	@Profile("security-mocked")
	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public SecurityServiceImpl securityService() {
			return Mockito.mock(SecurityServiceImpl.class);
		}

		@Bean
		@Primary
		public SampleListService service() {
			return Mockito.mock(SampleListService.class);
		}
	}


	@Autowired
	private SecurityServiceImpl securityService;

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		dto = new SampleListDto();
		dto.setDescription(DESCRIPTION);
		dto.setNotes(NOTES);
		dto.setCreatedBy(ADMIN);
		dto.setSelectionVariableId(8263);
		final List<Integer> instanceIds = new ArrayList<>();
		instanceIds.add(1);
		dto.setInstanceIds(instanceIds);
		dto.setTakenBy(ADMIN);
		dto.setSamplingDate("2017-08-01");
		dto.setStudyId(25025);
		dto.setCropName("maize");

		user = new User();
		user.setName(ADMIN);

		folderName = "Folder Name";
		parentId = 1;
		folderId = 2;
	}

	@Test
	public void createNewSampleList() throws Exception {
		final HashMap<String, Object> result = new HashMap<>();
		result.put("id", VALUE);
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/sample/maize/sampleList").build().encode();

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);
		Mockito.when(this.sampleListServiceMW.createOrUpdateSampleList(Mockito.any(SampleListDTO.class))).thenReturn(Integer.valueOf(VALUE));

		this.mockMvc.perform(
			MockMvcRequestBuilders.post(uriComponents.toUriString()).contentType(this.contentType).content(this.convertObjectToByte(dto)))
			.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(result.get("id"))));
	}

	@Test
	public void createSampleListFolder() throws Exception {
		final HashMap<String, Object> result = new HashMap<>();
		result.put("id", VALUE);

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);
		Mockito.when(this.sampleListServiceMW.createSampleListFolder(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString()))
			.thenReturn(Integer.valueOf(VALUE));

		String url = String.format("/sample/maize/sampleListFolder?folderName=%s&parentId=%s", folderName, parentId);
		this.mockMvc.perform(MockMvcRequestBuilders.post(url))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(result.get("id"))));
	}

	@Test
	public void updateSampleListFolder() throws Exception {
		final Integer folderId = 2;
		final Integer parentFolderId = 1;
		final String newFolderName = "NEW_NAME";
		final SampleList parentFolder = new SampleList();
		parentFolder.setId(parentFolderId);
		parentFolder.setType(SampleListType.FOLDER);
		final SampleList folder = new SampleList();
		folder.setId(folderId);
		folder.setHierarchy(parentFolder);
		folder.setType(SampleListType.FOLDER);

		String url = String.format("/sample/maize/sampleListFolder/{folderId}?newFolderName=%s", newFolderName);
		Mockito.when(this.sampleListServiceMW.updateSampleListFolderName(Mockito.anyInt(), Mockito.anyString())).thenReturn(folder);

		this.mockMvc.perform(MockMvcRequestBuilders.put(url, folderId)).
			andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(folder.getId().toString())));
	}


	@Test
	public void moveSampleListFolder() throws Exception {
		final Integer folderId = 2;
		final Integer parentFolderId = 1;
		final Integer newParentFolderId = 3;
		final SampleList parentFolder = new SampleList();
		parentFolder.setId(parentFolderId);
		parentFolder.setType(SampleListType.FOLDER);
		final SampleList folder = new SampleList();
		folder.setId(folderId);
		folder.setHierarchy(parentFolder);
		folder.setType(SampleListType.FOLDER);

		final SampleList newParentFolder = new SampleList();
		newParentFolder.setId(newParentFolderId);
		newParentFolder.setType(SampleListType.FOLDER);

		String url = String.format("/sample/maize/sampleListFolder/{folderId}/move?newParentId=%s", newParentFolderId);
		Mockito.when(this.sampleListServiceMW.moveSampleList(Mockito.anyInt(), Mockito.anyInt())).thenReturn(folder);


		this.mockMvc.perform(MockMvcRequestBuilders.put(url, folderId))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.jsonPath("$.parentId", Matchers.is(newParentFolderId.toString())))
		;
	}

	@Test
	public void deleteSampleListFolder() throws Exception {
		final String folderId = "2";
		final SampleList folder = new SampleList();
		folder.setId(Integer.valueOf(folderId));
		folder.setType(SampleListType.FOLDER);

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/sample/maize/sampleListFolder/{folderId}", folderId))
			.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is("0")))
			;
	}
}
