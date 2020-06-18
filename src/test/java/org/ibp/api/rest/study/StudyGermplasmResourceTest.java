package org.ibp.api.rest.study;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.study.StudyGermplasmService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Random;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class StudyGermplasmResourceTest extends ApiUnitTestBase {

    @Autowired
    private StudyGermplasmService studyGermplasmService;

    @Test
    public void testReplaceStudyGermplasm() throws Exception {


        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        final int newEntryId = random.nextInt();
        final StudyGermplasmDto newDto = new StudyGermplasmDto();
        newDto.setEntryId(random.nextInt());
        newDto.setGermplasmId(newGid);

        final StudyGermplasmDto dto = new StudyGermplasmDto(newEntryId, String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()), newGid, RandomStringUtils.randomAlphabetic(20), 6, RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));
        Mockito.doReturn(dto).when(this.studyGermplasmService).replaceStudyGermplasm(studyId, entryId, newDto);

        this.mockMvc.perform(MockMvcRequestBuilders
                .put("/crops/{cropname}/programs/{programUUID}/studies/{studyId}/germplasm/{entryId}", CropType.CropEnum.MAIZE.name().toLowerCase(), this.programUuid, studyId, entryId)
                .content(this.convertObjectToByte(newDto))
                .contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.entryId", Matchers.is(dto.getEntryId())))
                .andExpect(jsonPath("$.designation", Matchers.is(dto.getDesignation())))
                .andExpect(jsonPath("$.entryType", Matchers.is(dto.getEntryType())))
                .andExpect(jsonPath("$.germplasmId", Matchers.is(dto.getGermplasmId())))
                .andExpect(jsonPath("$.entryNumber", Matchers.is(dto.getEntryNumber())))
                .andExpect(jsonPath("$.entryCode", Matchers.is(dto.getEntryCode())))
                .andExpect(jsonPath("$.seedSource", Matchers.is(dto.getSeedSource())))
                .andExpect(jsonPath("$.cross", Matchers.is(dto.getCross())));
    }


}
