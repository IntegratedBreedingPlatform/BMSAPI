package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmNameBatchRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.service.api.GermplasmGroupNamingResult;

import java.util.Map;

public interface GermplasmNameService {

	public void deleteName(Integer gid, Integer nameId);

	public void updateName(String programUUID, GermplasmNameRequestDto germplasmNameRequestDto, Integer gid, Integer nameId);

	public Integer createName(String programUUID, GermplasmNameRequestDto germplasmNameRequestDto, Integer gid);

	public Map<Integer, GermplasmGroupNamingResult> createNames(String programUUID, GermplasmNameBatchRequestDto germplasmNameBatchRequestDto);

}
