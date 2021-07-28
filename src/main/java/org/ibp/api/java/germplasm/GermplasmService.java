
package org.ibp.api.java.germplasm;

import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmBasicDetailsDto;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.domain.germplasm.ProgenitorsDetailsDto;
import org.generationcp.middleware.domain.germplasm.ProgenitorsUpdateRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportResponseDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmMatchRequestDto;
import org.ibp.api.domain.germplasm.GermplasmDeleteResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GermplasmService {

	Integer DEFAULT_PEDIGREE_LEVELS = 20;

	int searchGermplasmCount(String searchText);

	List<GermplasmSearchResponse> searchGermplasm(GermplasmSearchRequest germplasmSearchRequest, Pageable pageable, String programUUID);

	long countSearchGermplasm(GermplasmSearchRequest germplasmSearchRequest, String programUUID);

	List<org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO> searchNameTypes(String name);

	Set<Integer> importGermplasmUpdates(String programUUID, List<GermplasmUpdateDTO> germplasmUpdateDTOList);

	List<GermplasmNameTypeDTO> filterGermplasmNameTypes(Set<String> codes);

	Map<Integer, GermplasmImportResponseDto> importGermplasm(String cropName, String programUUID,
		GermplasmImportRequestDto germplasmImportRequestDto);

	long countGermplasmMatches(GermplasmMatchRequestDto germplasmMatchRequestDto);

	List<GermplasmDto> findGermplasmMatches(GermplasmMatchRequestDto germplasmMatchRequestDto, Pageable pageable);

	GermplasmDeleteResponse deleteGermplasm(List<Integer> gids);

	GermplasmDto getGermplasmDtoById(Integer gid);

	ProgenitorsDetailsDto getGermplasmProgenitorDetails(Integer gid);

	boolean updateGermplasmBasicDetails(String programUUID, Integer gid, GermplasmBasicDetailsDto germplasmBasicDetailsDto);

	boolean updateGermplasmPedigree(final String programUUID, final Integer gid,
		final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto);

}
