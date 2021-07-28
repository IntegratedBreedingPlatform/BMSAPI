package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmCodeNameBatchRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.service.api.GermplasmCodingResult;

import java.util.List;

public interface GermplasmNameService {

	void deleteName(Integer gid, Integer nameId);

	void updateName(String programUUID, GermplasmNameRequestDto germplasmNameRequestDto, Integer gid, Integer nameId);

	Integer createName(String programUUID, GermplasmNameRequestDto germplasmNameRequestDto, Integer gid);

	List<GermplasmCodingResult> createCodeNames(GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto);

	String getNextNameInSequence(GermplasmNameSetting germplasmNameSetting);
}
