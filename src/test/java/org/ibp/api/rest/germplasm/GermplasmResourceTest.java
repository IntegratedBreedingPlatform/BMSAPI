package org.ibp.api.rest.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;

public class GermplasmResourceTest extends ApiUnitTestBase {

	@Test
	public void testImportGermplasmUpdateSuccess() throws Exception {

		final GermplasmUpdateDTO germplasmUpdateDTO = new GermplasmUpdateDTO();
		germplasmUpdateDTO.setGid(1);
		germplasmUpdateDTO.setGermplasmUUID("54c2a5cf-9b83-4f77-8b0d-c505b5c6e907");
		germplasmUpdateDTO.setPreferredName("DRVNM");
		germplasmUpdateDTO.setLocationAbbreviation("UKN");
		germplasmUpdateDTO.setCreationDate("20200101");
		germplasmUpdateDTO.setBreedingMethod("UBM");
		germplasmUpdateDTO.getNames().put("DRVNM", "Derivative Name");
		germplasmUpdateDTO.getAttributes().put("NOTE", "Note 1");

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.patch("/crops/{cropName}/germplasm", this.cropName)
				.contentType(this.contentType).content(this.convertObjectToByte(Arrays.asList(germplasmUpdateDTO))))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

	}

}
