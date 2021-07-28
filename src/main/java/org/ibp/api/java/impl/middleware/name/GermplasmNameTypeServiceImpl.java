package org.ibp.api.java.impl.middleware.name;

import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeRequestDTO;
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
	public List<GermplasmNameTypeDTO> getNameTypes(final Pageable pageable) {
		return this.germplasmNameTypeService.getNameTypes(pageable);
	}

	@Override
	public long countAllNameTypes() {
		return this.germplasmNameTypeService.countAllNameTypes();
	}

	@Override
	public Integer createNameType(final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO) {
		this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO, null);
		return this.germplasmNameTypeService.createNameType(germplasmNameTypeRequestDTO);
	}

	@Override
	public void updateNameType(final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO, final Integer nameTypeId) {
		this.germplasmNameTypeValidator.validate(germplasmNameTypeRequestDTO, nameTypeId);
		this.germplasmNameTypeService.updateNameType(germplasmNameTypeRequestDTO, nameTypeId);
	}

	@Override
	public void deleteNameType(final Integer nameTypeId) {
		this.germplasmNameTypeValidator.deletable(nameTypeId);
		this.germplasmNameTypeService.deleteNameType(nameTypeId);
	}
}
