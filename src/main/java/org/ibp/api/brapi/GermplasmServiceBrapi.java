package org.ibp.api.brapi;

import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.ibp.api.brapi.v2.germplasm.GermplasmImportResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplasmServiceBrapi {

	GermplasmImportResponse createGermplasm(String cropName, List<GermplasmImportRequest> germplasmImportRequestList);

	GermplasmDTO updateGermplasm(String germplasmDbId, GermplasmUpdateRequest germplasmUpdateRequest);

	List<GermplasmDTO> searchGermplasmDTO(GermplasmSearchRequestDto germplasmSearchRequestDTO, Pageable pageable);

	long countGermplasmDTOs(GermplasmSearchRequestDto germplasmSearchRequestDTO);

	PedigreeDTO getPedigree(String germplasmUUID, String notation, Boolean includeSiblings);

	ProgenyDTO getProgeny(String germplasmUUID);

	long countGermplasmByStudy(Integer studyDbId);

	List<GermplasmDTO> getGermplasmByStudy(int studyDbId, Pageable pageable);

	GermplasmDTO getGermplasmDTObyGUID(String germplasmUUID);

}
