package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmCodeNameBatchRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.service.api.GermplasmCodingResult;

import java.util.List;

public interface GermplasmNameService {

	public void deleteName(Integer gid, Integer nameId);

	public void updateName(String programUUID, GermplasmNameRequestDto germplasmNameRequestDto, Integer gid, Integer nameId);

	public Integer createName(String programUUID, GermplasmNameRequestDto germplasmNameRequestDto, Integer gid);

	public List<GermplasmCodingResult> createCodeNames(GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto);

	public String getNextNameInSequence(GermplasmNameSetting germplasmNameSetting);
}
