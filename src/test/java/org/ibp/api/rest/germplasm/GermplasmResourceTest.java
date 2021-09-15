package org.ibp.api.rest.germplasm;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;
import org.generationcp.middleware.domain.germplasm.GermplasmMergeRequestDto;
import org.ibp.api.domain.germplasm.GermplasmDeleteResponse;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class GermplasmResourceTest extends ApiUnitTestBase {

	@Resource
	private GermplasmService germplasmService;

	@Test
	public void testImportGermplasmUpdateSuccess() throws Exception {

		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setGid(1);
		germplasmUpdateDTO.setGermplasmUUID("54c2a5cf-9b83-4f77-8b0d-c505b5c6e907");
		germplasmUpdateDTO.setPreferredNameType("DRVNM");
		germplasmUpdateDTO.setLocationAbbreviation("UKN");
		germplasmUpdateDTO.setCreationDate("20200101");
		germplasmUpdateDTO.setBreedingMethodAbbr("UBM");
		germplasmUpdateDTO.getNames().put("DRVNM", "Derivative Name");
		germplasmUpdateDTO.getAttributes().put("NOTE", "Note 1");

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.patch("/crops/{cropName}/germplasm", this.cropName)
				.contentType(this.contentType).content(this.convertObjectToByte(Arrays.asList(germplasmUpdateDTO))))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

	}

	@Test
	public void testGermplasmDeleteSuccess() throws Exception {

		final List<Integer> gids = Lists.newArrayList(1, 2, 3);
		when(this.germplasmService.deleteGermplasm(gids))
			.thenReturn(new GermplasmDeleteResponse(Sets.newHashSet(1), Sets.newHashSet(2, 3)));

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.delete("/crops/{cropName}/germplasm", this.cropName)
				.param("gids", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(jsonPath("$.deletedGermplasm", Matchers.containsInAnyOrder(2, 3)))
			.andExpect(jsonPath("$.germplasmWithErrors", Matchers.containsInAnyOrder(1)));

	}

	@Test
	public void testGermplasmMergeSuccess() throws Exception {
		final GermplasmMergeRequestDto germplasmMergeRequestDto = new GermplasmMergeRequestDto();
		germplasmMergeRequestDto.setTargetGermplasmId(1);
		final GermplasmMergeRequestDto.NonSelectedGermplasm nonSelectedGermplasm = new GermplasmMergeRequestDto.NonSelectedGermplasm();
		nonSelectedGermplasm.setGermplasmId(2);
		nonSelectedGermplasm.setCloseLots(false);
		nonSelectedGermplasm.setOmit(false);
		nonSelectedGermplasm.setMigrateLots(false);
		final GermplasmMergeRequestDto.MergeOptions mergeOptions = new GermplasmMergeRequestDto.MergeOptions();
		mergeOptions.setMigrateAttributesData(false);
		mergeOptions.setMigrateNameTypes(false);
		mergeOptions.setMigratePassportData(false);
		germplasmMergeRequestDto.setNonSelectedGermplasm(Arrays.asList(nonSelectedGermplasm));
		germplasmMergeRequestDto.setMergeOptions(mergeOptions);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post("/crops/{cropName}/germplasm/merge", this.cropName)
				.contentType(this.contentType).content(this.convertObjectToByte(germplasmMergeRequestDto)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}

}
