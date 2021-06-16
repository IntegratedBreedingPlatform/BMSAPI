package org.ibp.api.java.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmCodeNameBatchRequestDto;
import org.generationcp.middleware.service.api.GermplasmCodingResult;

import java.util.List;

public interface GermplasmCodeGenerationService {

	List<GermplasmCodingResult> createCodeNames(GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto);

}
