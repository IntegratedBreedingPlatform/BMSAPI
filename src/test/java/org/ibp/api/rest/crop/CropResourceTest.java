package org.ibp.api.rest.crop;

import java.util.List;

import org.generationcp.middleware.pojos.workbench.CropType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class CropResourceTest extends ApiUnitTestBase {

	@Test
	public void listAvailableCrops() throws Exception {
		List<CropType> cropTypeList = Lists.newArrayList(new CropType("wheat"), new CropType("maize"));
		Mockito.when(this.workbenchDataManager.getInstalledCropDatabses()).thenReturn(cropTypeList);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crop/list").contentType(this.contentType)) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(cropTypeList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]", Matchers.is("wheat")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]", Matchers.is("maize")));
	}

}
