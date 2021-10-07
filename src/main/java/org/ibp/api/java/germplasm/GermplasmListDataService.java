package org.ibp.api.java.germplasm;

import org.generationcp.middleware.api.germplasmlist.GermplasmListColumnDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListMeasurementVariableDTO;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchResponse;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataUpdateViewDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplasmListDataService {

	List<GermplasmListDataSearchResponse> searchGermplasmListData(Integer listId, GermplasmListDataSearchRequest request, Pageable pageable);

	long countSearchGermplasmListData(Integer listId, GermplasmListDataSearchRequest request);

	List<GermplasmListColumnDTO> getGermplasmListColumns(Integer listId, final String programUUID);

	List<GermplasmListMeasurementVariableDTO> getGermplasmListDataTableHeader(Integer listId, final String programUUID);

	void saveGermplasmListDataView(final Integer listId, List<GermplasmListDataUpdateViewDTO> columns);

}
