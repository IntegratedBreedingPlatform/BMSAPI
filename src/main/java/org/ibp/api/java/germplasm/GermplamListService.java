package org.ibp.api.java.germplasm;

import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.pojos.GermplasmList;

import java.util.List;
import java.util.Map;

public interface GermplamListService {

	List<TreeNode> getGermplasmListChildrenNodes(final String crop, final String programUUID, final String parentId, final Boolean folderOnly);

	GermplasmList getGermplasmList(Integer germplasmListId);

	GermplasmListGeneratorDTO create(GermplasmListGeneratorDTO request);

	List<GermplasmListTypeDTO> getGermplasmListTypes();

	Map<String, Object> createGermplasmListFolder(final String cropName, String programUUID, String folderName, String parentId);

}
