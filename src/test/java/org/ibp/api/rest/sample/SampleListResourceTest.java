
package org.ibp.api.rest.sample;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
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
	private String programUUID;

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
			return Mockito.mock(SampleService.class);
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
		this.dto = new SampleListDto();
		this.dto.setDescription(SampleListResourceTest.DESCRIPTION);
		this.dto.setNotes(SampleListResourceTest.NOTES);
		this.dto.setCreatedBy(SampleListResourceTest.ADMIN);
		this.dto.setSelectionVariableId(8263);
		final List<Integer> instanceIds = new ArrayList<>();
		instanceIds.add(1);
		this.dto.setInstanceIds(instanceIds);
		this.dto.setTakenBy(SampleListResourceTest.ADMIN);
		this.dto.setSamplingDate("2017-08-01");
		this.dto.setStudyId(25025);
		this.dto.setCropName("maize");
		this.dto.setListName("SamplesTest");
		this.dto.setCreatedDate("2017-10-12");
		this.dto.setProgramUUID("c35c7769-bdad-4c70-a6c4-78c0dbf784e5");
		this.user = new User();
		this.user.setName(SampleListResourceTest.ADMIN);

		this.folderName = "Folder Name";
		this.parentId = 1;
		this.programUUID = "c35c7769-bdad-4c70-a6c4-78c0dbf784e5";

	}

	@Test
	public void createNewSampleList() throws Exception {
		final HashMap<String, Object> result = new HashMap<>();
		result.put("id", SampleListResourceTest.VALUE);
		final SampleList sampleList = new SampleList();
		sampleList.setId(Integer.valueOf(SampleListResourceTest.VALUE));
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/sampleLists/maize/sampleList").build().encode();

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(this.user);
		Mockito.when(this.sampleListServiceMW.createSampleList(org.mockito.Matchers.any(SampleListDTO.class))).thenReturn(sampleList);

		this.mockMvc
				.perform(MockMvcRequestBuilders.post(uriComponents.toUriString()).contentType(this.contentType)
						.content(this.convertObjectToByte(this.dto)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(result.get("id"))));
	}

	@Test
	// @Ignore
	public void createSampleListFolder() throws Exception {
		final HashMap<String, Object> result = new HashMap<>();
		result.put("id", SampleListResourceTest.VALUE);
		final User creatingBy = new User();
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(creatingBy);
		Mockito.when(this.sampleListServiceMW.createSampleListFolder(org.mockito.Matchers.anyString(), org.mockito.Matchers.anyInt(),
				org.mockito.Matchers.any(User.class), org.mockito.Matchers.anyString()))
				.thenReturn(Integer.valueOf(SampleListResourceTest.VALUE));

		final String url = String.format("/sampleLists/maize/sampleListFolder?folderName=%s&parentId=%s&programUUID=%s", this.folderName,
				this.parentId, this.programUUID);
		this.mockMvc.perform(MockMvcRequestBuilders.post(url)).andExpect(MockMvcResultMatchers.status().isOk())
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

		final String url = String.format("/sampleLists/maize/sampleListFolder/{folderId}?newFolderName=%s", newFolderName);
		Mockito.when(this.sampleListServiceMW.updateSampleListFolderName(org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyString()))
				.thenReturn(folder);

		this.mockMvc.perform(MockMvcRequestBuilders.put(url, folderId)).andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(folder.getId().toString())));
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

		final String url = String.format("/sampleLists/maize/sampleListFolder/{folderId}/move?newParentId=%s&isCropList=false", newParentFolderId);
		Mockito.when(this.sampleListServiceMW.moveSampleList(folderId, newParentFolderId, false)).thenReturn(folder);

		this.mockMvc.perform(MockMvcRequestBuilders.put(url, folderId)).andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.parentId", Matchers.is(folder.getHierarchy().getId().toString())));
	}

	@Test
	public void deleteSampleListFolder() throws Exception {
		final Integer folderId = 2;

		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(final InvocationOnMock invocation) {
				return null;
			}
		}).when(this.sampleListServiceMW).deleteSampleListFolder(folderId);

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/sampleLists/maize/sampleListFolder/{folderId}", folderId))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void testListSamples() throws Exception {
		final String plotId = RandomStringUtils.randomAlphanumeric(13);
		final Date samplingDate = SampleListResourceTest.DATE_FORMAT.parse("01/01/2018");
		final List<SampleDTO> list = new ArrayList<>();
		final SampleDTO sample = new SampleDTO(RandomStringUtils.randomAlphanumeric(6), RandomStringUtils.randomAlphanumeric(6),
				RandomStringUtils.randomAlphanumeric(6), samplingDate, RandomStringUtils.randomAlphanumeric(6), new Random().nextInt(),
				RandomStringUtils.randomAlphanumeric(6), new Random().nextInt());
		list.add(sample);

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(this.user);
		Mockito.when(this.sampleService.getSamples(plotId)).thenReturn(list);

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/sample/maize/samples?plotId=" + plotId).contentType(this.contentType)
						.content(this.convertObjectToByte(this.dto)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(list.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleName", Matchers.is(sample.getSampleName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleBusinessKey", Matchers.is(sample.getSampleBusinessKey())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].takenBy", Matchers.is(sample.getTakenBy())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].samplingDate",
						Matchers.is(SampleListResourceTest.DATE_FORMAT.format(sample.getSamplingDate()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleList", Matchers.is(sample.getSampleList())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].plantNumber", Matchers.is(sample.getPlantNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].plantBusinessKey", Matchers.is(sample.getPlantBusinessKey())));
	}

	@Test
	public void testListSamplesNotFound() throws Exception {
		final String plotId = null;

		final List<SampleDTO> list = new ArrayList<>();

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(this.user);
		Mockito.when(this.sampleService.getSamples(plotId)).thenReturn(list);

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/sample/maize/samples?plotId=" + plotId).contentType(this.contentType)
						.content(this.convertObjectToByte(this.dto)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.empty()));
	}
}
