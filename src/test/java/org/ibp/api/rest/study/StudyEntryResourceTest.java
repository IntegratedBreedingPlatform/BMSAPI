package org.ibp.api.rest.study;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyEntryListGeneratorRequestDto;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.study.StudyEntryService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class StudyEntryResourceTest extends ApiUnitTestBase {

	@Autowired
	private StudyEntryService studyEntryService;

	@Test
	public void testReplaceStudyEntry() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt();
		final int entryId = random.nextInt();
		final int newGid = random.nextInt();
		final int newEntryId = random.nextInt();
		final StudyEntryDto newDto = new StudyEntryDto();
		newDto.setEntryId(random.nextInt());
		newDto.setGid(newGid);

		final StudyEntryDto dto = new StudyEntryDto(newEntryId, 6, RandomStringUtils.randomAlphabetic(20), newGid, RandomStringUtils.randomAlphabetic(20));
		dto.getProperties().put(TermId.ENTRY_TYPE.getId(), new StudyEntryPropertyData(String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())));
		Mockito.doReturn(dto).when(this.studyEntryService).replaceStudyEntry(studyId, entryId, newDto);

		this.mockMvc.perform(MockMvcRequestBuilders
			.put("/crops/{cropname}/programs/{programUUID}/studies/{studyId}/entries/{entryId}",
				CropType.CropEnum.MAIZE.name().toLowerCase(), this.programUuid, studyId, entryId)
			.content(this.convertObjectToByte(newDto))
			.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(jsonPath("$.entryId", Matchers.is(dto.getEntryId())))
			.andExpect(jsonPath("$.designation", Matchers.is(dto.getDesignation())))
			.andExpect(jsonPath("$.gid", Matchers.is(dto.getGid())))
			.andExpect(jsonPath("$.entryNumber", Matchers.is(dto.getEntryNumber())))
			.andExpect(jsonPath("$.entryCode", Matchers.is(dto.getEntryCode())));

	}

	@Test
	public void testCreateStudyGermplasmList() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt();
		final int germplasmListId = random.nextInt();
		final int entryId = random.nextInt();
		final int gid = random.nextInt();

		final StudyEntryListGeneratorRequestDto studyEntryListGeneratorRequestDto = new StudyEntryListGeneratorRequestDto();
		studyEntryListGeneratorRequestDto.setListId(germplasmListId);

		final StudyEntryDto dto = new StudyEntryDto(entryId, 6, RandomStringUtils.randomAlphabetic(20), gid, RandomStringUtils.randomAlphabetic(20));
		final List<StudyEntryDto> studyEntries = new ArrayList<>();
		studyEntries.add(dto);
		Mockito.doReturn(studyEntries).when(this.studyEntryService).createStudyEntries(studyId, germplasmListId);

		this.mockMvc.perform(MockMvcRequestBuilders
			.post("/crops/{cropname}/programs/{programUUID}/studies/{studyId}/entries/generation",
				CropType.CropEnum.MAIZE.name().toLowerCase(), this.programUuid, studyId, entryId)
			.content(this.convertObjectToByte(studyEntryListGeneratorRequestDto))
			.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(jsonPath("$[0].entryId", Matchers.is(dto.getEntryId())))
			.andExpect(jsonPath("$[0].designation", Matchers.is(dto.getDesignation())))
			.andExpect(jsonPath("$[0].gid", Matchers.is(dto.getGid())))
			.andExpect(jsonPath("$[0].entryNumber", Matchers.is(dto.getEntryNumber())))
			.andExpect(jsonPath("$[0].entryCode", Matchers.is(dto.getEntryCode())));

	}

	@Test
	public void testDeleteStudyGermplasm() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt();

		this.mockMvc.perform(MockMvcRequestBuilders
			.delete("/crops/{cropname}/programs/{programUUID}/studies/{studyId}/entries",
				CropType.CropEnum.MAIZE.name().toLowerCase(), this.programUuid, studyId)
			.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNoContent());

	}

	@Test
	public void testUpdateStudyEntryProperty() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt();
		final int entryId = random.nextInt();
		final int propertyId = random.nextInt();

		final StudyEntryPropertyData studyEntryPropertyData = new StudyEntryPropertyData();

		this.mockMvc.perform(MockMvcRequestBuilders
			.put("/crops/{cropname}/programs/{programUUID}/studies/{studyId}/entries/{entryId}/properties/{propertyId}",
				CropType.CropEnum.MAIZE.name().toLowerCase(), this.programUuid, studyId, entryId, propertyId)
			.content(this.convertObjectToByte(studyEntryPropertyData))
			.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNoContent());

	}

}
