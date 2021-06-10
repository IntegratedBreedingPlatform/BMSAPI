package org.ibp.api.rest.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.springframework.http.ResponseEntity;

public interface GermplasmAPI {

	ResponseEntity<GermplasmDto> getGermplasmDtoById(final String cropName, final Integer gid, final String programUUID,
		final String token);

}
