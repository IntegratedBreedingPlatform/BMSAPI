
package org.ibp.api.rest.germplasm;

import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.data.initializer.GermplasmTestDataProvider;
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

public class GermplasmResourcePedigreeTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public PedigreeDataManager pedigreeDataManager() {
			return Mockito.mock(PedigreeDataManager.class);
		}
	}

	@Autowired
	private PedigreeDataManager pedigreeDataManager;

	@Test
	public void testGetPedigreeTree() throws Exception {

		GermplasmPedigreeTree tree = new GermplasmPedigreeTree();
		String mainNodeName = "Test Germplasm", parent1Name = "Test Gerplasm Male Parent", parent2Name = "Test Gerplasm Female Parent";
		Integer mainNodeGid = 3, parent1Gid = 2, parent2Gid = 1;
		GermplasmPedigreeTreeNode rootNode = GermplasmTestDataProvider.createNode(mainNodeGid, mainNodeName, parent1Gid, parent1Name, parent2Gid, parent2Name);
		tree.setRoot(rootNode);

		Mockito.when(this.pedigreeDataManager.generatePedigreeTree(Mockito.anyInt(), Mockito.anyInt())).thenReturn(tree);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/germplasm/maize/pedigree/123").contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.root", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.germplasmId", Matchers.is(mainNodeGid.toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.name", Matchers.is(mainNodeName)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.parents", IsCollectionWithSize.hasSize(2)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.parents[0].germplasmId", Matchers.is(parent1Gid.toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.parents[0].name", Matchers.is(parent1Name)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.parents[1].germplasmId", Matchers.is(parent2Gid.toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.parents[1].name", Matchers.is(parent2Name)));
	}
}
