package org.ibp.api.java.germplasm;

import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.api.germplasmlist.MyListsDTO;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchRequest;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchResponse;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.rest.common.UserTreeState;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplasmListService {

	List<TreeNode> getGermplasmListChildrenNodes(
		final String crop, final String programUUID, final String parentId, final Boolean folderOnly);

	List<TreeNode> getUserTreeState(final String crop, final String programUUID, final String userId);

	void saveGermplasmListTreeState(final String crop, final String programUUID, final UserTreeState userTreeState);

	GermplasmList getGermplasmList(Integer germplasmListId);

	long countMyLists(String programUUID, Integer userId);

	List<MyListsDTO> getMyLists(String programUUID, Pageable pageable, Integer userId);

	GermplasmListGeneratorDTO create(GermplasmListGeneratorDTO request);

	void importUpdates(GermplasmListGeneratorDTO request);

	List<GermplasmListTypeDTO> getGermplasmListTypes();

	void addGermplasmEntriesToList(
		Integer germplasmListId, SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite,
		String programUUID);

	Integer createGermplasmListFolder(String cropName, String programUUID, String folderName, String parentId);

	Integer updateGermplasmListFolderName(String cropName, String programUUID, String newFolderName, String folderId);

	Integer moveGermplasmListFolder(String cropName, String programUUID, String folderId, String newParentFolderId);

	void deleteGermplasmListFolder(String cropName, String programUUID, String folderId);

	GermplasmListDto getGermplasmListById(Integer listId);

	List<GermplasmListDto> getGermplasmLists(Integer gid);

	List<GermplasmListSearchResponse> searchGermplasmList(GermplasmListSearchRequest request, Pageable pageable, String programUUID);

	List<Variable> getGermplasmListVariables(String programUUID, Integer listId, Integer variableTypeId);

	long countSearchGermplasmList(GermplasmListSearchRequest request, String programUUID);

	boolean toggleGermplasmListStatus(Integer listId);

	void deleteGermplasmList(String cropName, String programUUID, Integer listId);

	void addGermplasmListEntriesToAnotherList(String cropName, String programUUID, Integer destinationListId, Integer sourceListId,
		SearchCompositeDto<GermplasmListDataSearchRequest, Integer> searchComposite);

	void removeGermplasmEntriesFromList(Integer germplasmListId, SearchCompositeDto<GermplasmListDataSearchRequest, Integer> searchComposite);

	void editListMetadata(GermplasmListDto germplasmListDto);
}
