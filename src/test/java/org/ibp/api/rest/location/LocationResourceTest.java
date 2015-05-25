
package org.ibp.api.rest.location;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class LocationResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public WorkbenchDataManager workbenchDataManager() {
			return Mockito.mock(WorkbenchDataManager.class);
		}

		@Bean
		@Primary
		public LocationDataManager locationDataManager() {
			return Mockito.mock(LocationDataManager.class);
		}
	}

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private LocationDataManager locationDataManager;

	@Test
	public void testGetAllLocationTypes() throws Exception {

		UserDefinedField udfld1 = new UserDefinedField();
		udfld1.setFldno(415);
		udfld1.setFcode("FIELD");
		udfld1.setFname("EXPERIMENTAL FIELD");

		UserDefinedField udfld2 = new UserDefinedField();
		udfld2.setFldno(416);
		udfld2.setFcode("BLOCK");
		udfld2.setFname("FIELD BLOCK");

		List<UserDefinedField> mwLocTypes = new ArrayList<>();
		mwLocTypes.add(udfld1);
		mwLocTypes.add(udfld2);

		Mockito.when(this.locationDataManager.getUserDefinedFieldByFieldTableNameAndType(UDTableType.LOCATION_LTYPE.getTable(), UDTableType.LOCATION_LTYPE.getType())).thenReturn(mwLocTypes);
		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/location/{cropname}/types", "maize")
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(mwLocTypes.size())))
				
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['id']", Matchers.is(udfld1.getFldno().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['name']", Matchers.is(udfld1.getFcode())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['description']", Matchers.is(udfld1.getFname())))
				
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]['id']", Matchers.is(udfld2.getFldno().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]['name']", Matchers.is(udfld2.getFcode())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[1]['description']", Matchers.is(udfld2.getFname())));
	}
}
