package org.ibp.api.java.germplasm;

import org.generationcp.middleware.api.germplasmlist.GermplasmListObservationRequestDto;

import java.util.List;

public interface GermplasmListObservationService {

	Integer create(String programUUID, Integer listId, GermplasmListObservationRequestDto germplasmListObservationRequestDto);

	void update(String programUUID, Integer listId, Integer observationId, String value);

	void delete(Integer listId, Integer observationId);

	long countObservationsByVariables(Integer listId, List<Integer> variableIds);
}
