package org.ibp.api.rest.sample;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.enumeration.SampleListType;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.service.api.SampleListService;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.ibp.api.java.impl.middleware.security.SecurityServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import static org.mockito.Mockito.doAnswer;

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

	private static final SimpleDateFormat DATE_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT_3);

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

		@Bean
		@Primary
		public SampleService sampleService() {
			return  Mockito.mock(SampleService.class);
		}
	}


	@Autowired
	private SecurityServiceImpl securityService;

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	@Autowired
	private SampleService sampleService;

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
	}

	@Test
	public void createNewSampleList() throws Exception {
		final HashMap<String, Object> result = new HashMap<>();
		result.put("id", VALUE);
		SampleList sampleList = new SampleList();
		sampleList.setId(Integer.valueOf(VALUE));
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/sampleLists/maize/sampleList").build().encode();

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);
		Mockito.when(this.sampleListServiceMW.createSampleList(Mockito.any(SampleListDTO.class))).thenReturn(sampleList);

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

		String url = String.format("/sampleLists/maize/sampleListFolder?folderName=%s&parentId=%s", folderName, parentId);
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

		String url = String.format("/sampleLists/maize/sampleListFolder/{folderId}?newFolderName=%s", newFolderName);
		Mockito.when(this.sampleListServiceMW.updateSampleListFolderName(Mockito.anyInt(), Mockito.anyString())).thenReturn(folder);

		this.mockMvc.perform(MockMvcRequestBuilders.put(url, folderId)).
			andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(folder.getId().toString())));
	}


	@Test
	public void moveSampleListFolder() throws Exception {
		final Integer folderId = 2;

		final Integer newParentFolderId = 3;

		final SampleList parentFolder = new SampleList();
		parentFolder.setId(newParentFolderId);
		parentFolder.setType(SampleListType.FOLDER);

		final SampleList folder = new SampleList();
		folder.setId(folderId);
		folder.setHierarchy(parentFolder);
		folder.setType(SampleListType.FOLDER);


		final String url = String.format("/sampleLists/maize/sampleListFolder/{folderId}/move?newParentId=%s", newParentFolderId);
		Mockito.when(this.sampleListServiceMW.moveSampleList(folderId, newParentFolderId)).thenReturn(folder);


		this.mockMvc.perform(MockMvcRequestBuilders.put(url, folderId))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.jsonPath("$.parentId", Matchers.is(
				folder.getHierarchy().getId().toString())))
		;
	}

	@Test
	public void deleteSampleListFolder() throws Exception {
		final Integer folderId = 2;

		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				return null;
			}
		}).when(this.sampleListServiceMW).deleteSampleListFolder(folderId);

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/sampleLists/maize/sampleListFolder/{folderId}", folderId))
			.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is("0")))
			;
	}

	@Test
	public void testListSamples() throws Exception {
		String plotId = randomAlphanumeric(13);

		List<SampleDTO> list = new ArrayList<>();
		SampleDTO sample =
			new SampleDTO(randomAlphanumeric(6), randomAlphanumeric(6), randomAlphanumeric(6), new Date(), randomAlphanumeric(6),
				new Random().nextInt(), randomAlphanumeric(6));
		list.add(sample);

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);
		Mockito.when(this.sampleService.getSamples(plotId)).thenReturn(list);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/sample/maize/samples?plotId=" + plotId)
			.contentType(this.contentType)
			.content(this.convertObjectToByte(dto)))
			.andExpect(MockMvcResultMatchers.status()
				.isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(list.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleName", Matchers.is(sample.getSampleName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleBusinessKey", Matchers.is(sample.getSampleBusinessKey())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].takenBy", Matchers.is(sample.getTakenBy())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].samplingDate", Matchers.is(DATE_FORMAT.format(sample.getSamplingDate()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleList", Matchers.is(sample.getSampleList())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].plantNumber", Matchers.is(sample.getPlantNumber())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].plantBusinessKey", Matchers.is(sample.getPlantBusinessKey())))
		;
	}

	@Test
	public void testListSamplesNotFound() throws Exception {
		String plotId = null;

		List<SampleDTO> list = new ArrayList<>();

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);
		Mockito.when(this.sampleService.getSamples(plotId)).thenReturn(list);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/sample/maize/samples?plotId=" + plotId)
			.contentType(this.contentType)
			.content(this.convertObjectToByte(dto)))
			.andExpect(MockMvcResultMatchers.status()
				.isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.empty()))
			;
	}
}
