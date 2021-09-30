package org.ibp.api.java.germplasm;

import org.generationcp.middleware.api.germplasmlist.GermplasmListObservationRequestDto;

public interface GermplasmListObservationService {

	Integer create(Integer listId, GermplasmListObservationRequestDto germplasmListObservationRequestDto);

	void update(Integer listId, Integer observationId, String value);

	void delete(Integer listId, Integer observationId);

}
