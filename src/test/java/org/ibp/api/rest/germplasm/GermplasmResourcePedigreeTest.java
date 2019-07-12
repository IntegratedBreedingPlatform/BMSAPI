
package org.ibp.api.rest.germplasm;

import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.germplasm.PedigreeTree;
import org.ibp.api.domain.germplasm.PedigreeTreeNode;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class GermplasmResourcePedigreeTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {
		@Bean
		@Primary
		public GermplasmService germplasmService() {
			return Mockito.mock(GermplasmService.class);
		}
	}

	@Autowired
	private GermplasmService germplasmService;

	@Test
	public void testGetPedigreeTree() throws Exception {

		PedigreeTree tree = new PedigreeTree();
		String mainNodeName = "Test Germplasm", parent1Name = "Test Gerplasm Male Parent", parent2Name = "Test Gerplasm Female Parent";
		Integer mainNodeGid = 3, parent1Gid = 2, parent2Gid = 1;
		PedigreeTreeNode rootNode = this.createNode(mainNodeGid, mainNodeName, parent1Gid, parent1Name, parent2Gid, parent2Name);
		tree.setRoot(rootNode);

		Mockito.doReturn(tree).when(this.germplasmService).getPedigreeTree("123", null);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/germplasm/maize/pedigree/123").contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.root", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.germplasmId", Matchers.is(mainNodeGid.toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.name", Matchers.is(mainNodeName)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.children", IsCollectionWithSize.hasSize(2)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.children[0].germplasmId", Matchers.is(parent1Gid.toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.children[0].name", Matchers.is(parent1Name)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.children[1].germplasmId", Matchers.is(parent2Gid.toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.root.children[1].name", Matchers.is(parent2Name)));
	}

	private PedigreeTreeNode createNode(Integer gid, String name, Integer parent1Gid, String parent1Name, Integer parent2Gid,
			String parent2Name) {
		PedigreeTreeNode mainNode = new PedigreeTreeNode();
		mainNode.setGermplasmId(gid.toString());
		mainNode.setName(name);

		PedigreeTreeNode parent1Node = new PedigreeTreeNode();
		parent1Node.setGermplasmId(parent1Gid.toString());
		parent1Node.setName(parent1Name);

		PedigreeTreeNode parent2Node = new PedigreeTreeNode();
		parent2Node.setGermplasmId(parent2Gid.toString());
		parent2Node.setName(parent2Name);

		mainNode.setParents(Lists.newArrayList(parent1Node, parent2Node));

		return mainNode;
	}

}
