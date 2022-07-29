package org.ibp.api.brapi.v1.observation;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ObservationResourceBrapiTest extends ApiUnitTestBase {

	private static final Locale locale = Locale.getDefault();


	@Autowired
	private DatasetTypeService datasetTypeService;
	

	@Test
	public void testGetObservationLevels() throws Exception {
		
		final List<String> observationLevels = new ArrayList<>();
		observationLevels.add("PLOT");
		observationLevels.add("MEANS");
		observationLevels.add("PLANT");

		Mockito.when(this.datasetTypeService.countObservationLevels()).thenReturn((long) observationLevels.size());
		Mockito.when(this.datasetTypeService
			.getObservationLevels(Mockito.eq(BrapiPagedResult.DEFAULT_PAGE_SIZE),Mockito.eq(BrapiPagedResult.DEFAULT_PAGE_NUMBER + 1)))
			.thenReturn(observationLevels);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/observationlevels")
			.contentType(this.contentType)
			.locale(locale))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(observationLevels.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0]",
				Matchers.is(observationLevels.get(0))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1]",
				Matchers.is(observationLevels.get(1))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[2]",
				Matchers.is(observationLevels.get(2))));

	}
}
