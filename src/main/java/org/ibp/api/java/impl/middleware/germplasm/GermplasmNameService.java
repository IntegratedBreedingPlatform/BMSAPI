package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;

public interface GermplasmNameService {

	public void deleteName(GermplasmNameRequestDto germplasmNameRequestDto);

	public void updateName(String programUUID, GermplasmNameRequestDto germplasmNameRequestDto);

	public Integer createName(String programUUID, GermplasmNameRequestDto germplasmNameRequestDto);

}
