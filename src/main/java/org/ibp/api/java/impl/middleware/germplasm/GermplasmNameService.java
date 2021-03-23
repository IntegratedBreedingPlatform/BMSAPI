package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;

public interface GermplasmNameService {

	public void deleteName(GermplasmNameRequestDto germplasmNameRequestDto);

	public void updateName(GermplasmNameRequestDto germplasmNameRequestDto, final String programUUID);

	public Integer createName(GermplasmNameRequestDto germplasmNameRequestDto, final String programUUID);

}
