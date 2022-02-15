package org.ibp.api.rest.germplasmlist;

import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.germplasm.GermplasmListService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Random;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.Mockito.doReturn;

public class GermplasmListResourceTest extends ApiUnitTestBase {

	private static final String PROGRAM_UUID = "50a7e02e-db60-4240-bd64-417b34606e46";

	@Resource
	private GermplasmListService germplasmListService;

	@Test
	public void testCloneGermplasmList() throws Exception {
		final String crop = CropType.CropEnum.MAIZE.name().toLowerCase();
		final Integer listId = new Random().nextInt(100);

		final GermplasmListDto request = new GermplasmListDto();
		request.setListName(randomAlphanumeric(10));

		final GermplasmListDto resultList = new GermplasmListDto();
		resultList.setListName(request.getListName());
		resultList.setListId(1);

		doReturn(resultList).when(this.germplasmListService).clone(Mockito.anyInt(), Mockito.any(GermplasmListDto.class));

		this.mockMvc.perform(MockMvcRequestBuilders.post("/crops/{cropName}/germplasm-lists/{listId}/clone",
					crop, listId).param("programUUID", GermplasmListResourceTest.PROGRAM_UUID)
				.content(this.convertObjectToByte(request))
				.contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isCreated())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.listName",
				Matchers.is(request.getListName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.listId",
				Matchers.is(resultList.getListId())));

	}

}
