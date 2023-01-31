package org.ibp.api.rest.germplasmlist;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.germplasm.GermplasmListTreeService;
import org.ibp.api.java.impl.middleware.germplasm.GermplasmListTreeServiceImpl;
import org.ibp.api.rest.common.UserTreeState;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doReturn;

public class GermplasmListTreeResourceTest extends ApiUnitTestBase {

	private static final String PROGRAM_UUID = "50a7e02e-db60-4240-bd64-417b34606e46";

	@Resource
	private GermplasmListTreeService germplasmListTreeService;

	@Test
	public void testGetUserTreeState() throws Exception {

		final String crop = CropType.CropEnum.MAIZE.name().toLowerCase();
		final List<TreeNode> list = this.getTreeNodes();
		doReturn(list).when(this.germplasmListTreeService).getUserTreeState(crop, GermplasmListTreeResourceTest.PROGRAM_UUID);

		final TreeNode firstNode = list.get(0);
		final TreeNode secondNode = list.get(1);
		final TreeNode childNode = list.get(1).getChildren().get(0);
		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{crop}/germplasm-lists/tree-state",
			crop).param("programUUID", GermplasmListTreeResourceTest.PROGRAM_UUID)
			.contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(list.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].key",
				Matchers.is(firstNode.getKey())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].title",
				Matchers.is(firstNode.getTitle())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].isFolder",
				Matchers.is(true)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].programUUID",
				Matchers.nullValue()))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].children", IsCollectionWithSize.hasSize(0)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].key",
				Matchers.is(secondNode.getKey())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].title",
				Matchers.is(secondNode.getTitle())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].isFolder",
				Matchers.is(true)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].programUUID",
				Matchers.is(GermplasmListTreeResourceTest.PROGRAM_UUID)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].children", IsCollectionWithSize.hasSize(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].children[0].key", Matchers.is(childNode.getKey())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].children[0].title", Matchers.is(childNode.getTitle())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].children[0].isFolder", Matchers.is(true)))
			.andExpect(
				MockMvcResultMatchers.jsonPath("$[1].children[0].programUUID", Matchers.is(GermplasmListTreeResourceTest.PROGRAM_UUID)));

	}

	@Test
	public void testSaveUserTreeState() throws Exception {
		final String crop = CropType.CropEnum.MAIZE.name().toLowerCase();
		final UserTreeState treeState = new UserTreeState();
		treeState.setProgramFolders(Lists.newArrayList(GermplasmListTreeServiceImpl.PROGRAM_LISTS, "5", "7"));
		treeState.setCropFolders(Lists.newArrayList(GermplasmListTreeServiceImpl.CROP_LISTS, "15", "17"));
		this.mockMvc.perform(MockMvcRequestBuilders.post("/crops/{crop}/germplasm-lists/tree-state",
			crop).param("programUUID", GermplasmListTreeResourceTest.PROGRAM_UUID).content(this.convertObjectToByte(treeState))
			.contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print());

	}

	private List<TreeNode> getTreeNodes() {
		final TreeNode cropFolderNode = new TreeNode(GermplasmListTreeServiceImpl.CROP_LISTS, AppConstants.CROP_LISTS.getString(), true, "",
			AppConstants.FOLDER_ICON_PNG.getString(), null);
		final TreeNode programNode = new TreeNode(GermplasmListTreeServiceImpl.PROGRAM_LISTS, AppConstants.PROGRAM_LISTS.getString(), true, "",
			AppConstants.FOLDER_ICON_PNG.getString(), GermplasmListTreeResourceTest.PROGRAM_UUID);
		programNode.setChildren(
			Collections.singletonList(new TreeNode(RandomStringUtils.randomNumeric(2), RandomStringUtils.randomAlphabetic(20), true, "",
				AppConstants.FOLDER_ICON_PNG.getString(), GermplasmListTreeResourceTest.PROGRAM_UUID)));
		return Arrays.asList(cropFolderNode, programNode);
	}

}
