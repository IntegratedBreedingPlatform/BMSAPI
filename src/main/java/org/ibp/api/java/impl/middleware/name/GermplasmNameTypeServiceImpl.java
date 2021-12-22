package org.ibp.api.java.impl.middleware.name;

import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeRequestDTO;
import org.generationcp.middleware.api.nametype.NameTypeMetadataFilterRequest;
import org.ibp.api.java.impl.middleware.name.validator.GermplasmNameTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GermplasmNameTypeServiceImpl implements GermplasmNameTypeService {

	@Autowired
	private org.generationcp.middleware.api.nametype.GermplasmNameTypeService germplasmNameTypeService;

	@Autowired
	private GermplasmNameTypeValidator germplasmNameTypeValidator;

	@Override
	public List<GermplasmNameTypeDTO> searchNameTypes(final NameTypeMetadataFilterRequest nameTypeMetadataFilterRequest, final Pageable pageable) {
		return this.germplasmNameTypeService.searchNameTypes(nameTypeMetadataFilterRequest, pageable);
	}

	@Override
	public long countSearchNameTypes(final NameTypeMetadataFilterRequest nameTypeMetadataFilterRequest) {
		return this.germplasmNameTypeService.countSearchNameTypes(nameTypeMetadataFilterRequest);
	}

	@Override
	public Integer createNameType(final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO) {
		this.germplasmNameTypeValidator.validateNameTypeCreation(germplasmNameTypeRequestDTO);
		return this.germplasmNameTypeService.createNameType(germplasmNameTypeRequestDTO);
	}

	@Override
	public boolean updateNameType(final Integer nameTypeId, final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO) {
		this.germplasmNameTypeValidator.validateNameTypeModification(nameTypeId, germplasmNameTypeRequestDTO);
		if (germplasmNameTypeRequestDTO.allAttributesNull()) {
			return false;
		}
		this.germplasmNameTypeService.updateNameType(nameTypeId, germplasmNameTypeRequestDTO);
		return true;
	}

	@Override
	public void deleteNameType(final Integer nameTypeId) {
		this.germplasmNameTypeValidator.validateNameTypeDeletion(nameTypeId);
		this.germplasmNameTypeService.deleteNameType(nameTypeId);
	}
}
