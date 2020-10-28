
package org.ibp.api.java.germplasm;

import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.pojos.Method;
import org.ibp.api.domain.germplasm.DescendantTree;
import org.ibp.api.domain.germplasm.GermplasmName;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.germplasm.PedigreeTree;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplasmService {

	Integer DEFAULT_PEDIGREE_LEVELS = 20;

	int searchGermplasmCount(String searchText);

	List<GermplasmSearchResponse> searchGermplasm(final GermplasmSearchRequest germplasmSearchRequest, final Pageable pageable,
		final String programUUID);

	long countSearchGermplasm(GermplasmSearchRequest germplasmSearchRequest, String programUUID);

	GermplasmSummary getGermplasm(String germplasmId);

	List<org.generationcp.middleware.api.attribute.AttributeDTO> searchAttributes(String name);

	PedigreeDTO getPedigree(Integer germplasmDbId, String notation, Boolean includeSiblings);

	ProgenyDTO getProgeny(Integer germplasmDbId);

	PedigreeTree getPedigreeTree(String germplasmId, Integer levels);

	DescendantTree getDescendantTree(String germplasmId);

	GermplasmDTO getGermplasmDTObyGID (Integer germplasmId);

	List<GermplasmDTO> searchGermplasmDTO (GermplasmSearchRequestDto germplasmSearchRequestDTO, Integer page, Integer pageSize);

	long countGermplasmDTOs(GermplasmSearchRequestDto germplasmSearchRequestDTO);

	long countGermplasmByStudy(Integer studyDbId);

	List<GermplasmDTO> getGermplasmByStudy(int studyDbId, int pageSize, int pageNumber);

	List<AttributeDTO> getAttributesByGid(
		String gid, List<String> attributeDbIds, Integer pageSize, Integer pageNUmber);

	long countAttributesByGid(String gid, List<String> attributeDbIds);

	List<AttributeDTO> getGermplasmAttributes();

	List<Method> getAllBreedingMethods();

	List<GermplasmName> getGermplasmNames();
}
