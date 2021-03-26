
package org.ibp.api.java.germplasm;

import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.GermplasmPatchDto;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenitorsDetailsDto;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportResponseDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmMatchRequestDto;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.ibp.api.brapi.v2.germplasm.GermplasmImportResponse;
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

	List<org.generationcp.middleware.api.attribute.AttributeDTO> searchAttributes(String name);

	List<org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO> searchNameTypes(String name);

	PedigreeDTO getPedigree(String germplasmUUID, String notation, Boolean includeSiblings);

	ProgenyDTO getProgeny(String germplasmUUID);

	GermplasmDTO getGermplasmDTObyGUID(String germplasmUUID);

	List<GermplasmDTO> searchGermplasmDTO(GermplasmSearchRequestDto germplasmSearchRequestDTO, Pageable pageable);

	long countGermplasmDTOs(GermplasmSearchRequestDto germplasmSearchRequestDTO);

	long countGermplasmByStudy(Integer studyDbId);

	List<GermplasmDTO> getGermplasmByStudy(int studyDbId, Pageable pageable);

	List<AttributeDTO> getAttributesByGUID(
		String germplasmUUID, List<String> attributeDbIds, Pageable pageable);

	long countAttributesByGUID(String germplasmUUID, List<String> attributeDbIds);

	Set<Integer> importGermplasmUpdates(String programUUID, List<GermplasmUpdateDTO> germplasmUpdateDTOList);

	List<GermplasmNameTypeDTO> filterGermplasmNameTypes(Set<String> codes);

	Map<Integer, GermplasmImportResponseDto> importGermplasm(String cropName, String programUUID,
		GermplasmImportRequestDto germplasmImportRequestDto);

	long countGermplasmMatches(GermplasmMatchRequestDto germplasmMatchRequestDto);

	List<GermplasmDto> findGermplasmMatches(GermplasmMatchRequestDto germplasmMatchRequestDto, Pageable pageable);

	GermplasmImportResponse createGermplasm(final String cropName, final List<GermplasmImportRequest> germplasmImportRequestList);

	GermplasmDTO updateGermplasm(final String germplasmUUID, final GermplasmUpdateRequest germplasmUpdateRequest);

	GermplasmDeleteResponse deleteGermplasm(List<Integer> gids);

	GermplasmDto getGermplasmDtoById(Integer gid);

	ProgenitorsDetailsDto getGermplasmProgenitorDetails(Integer gid);

	void updateGermplasm(final String programUUID, final Integer gid, final GermplasmPatchDto germplasmPatchDto);

}
