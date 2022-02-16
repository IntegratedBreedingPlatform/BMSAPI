package org.ibp.api.java.germplasm;

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
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplasmListService {

	GermplasmList getGermplasmList(Integer germplasmListId);

	long countMyLists(String programUUID, Integer userId);

	List<MyListsDTO> getMyLists(String programUUID, Pageable pageable, Integer userId);

	GermplasmListDto clone(Integer germplasmListId, GermplasmListDto request);

	GermplasmListGeneratorDTO create(GermplasmListGeneratorDTO request);

	void importUpdates(GermplasmListGeneratorDTO request);

	List<GermplasmListTypeDTO> getGermplasmListTypes();

	void addGermplasmEntriesToList(
		Integer germplasmListId, SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite,
		String programUUID);

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

	void editListMetadata(GermplasmListDto germplasmListDto, String programUUID);
}
