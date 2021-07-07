package org.ibp.api.java.impl.middleware.name;

import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeRequestDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.ibp.api.java.impl.middleware.name.validator.NameTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NameTypeServiceImpl implements NameTypeService {

	@Autowired
	GermplasmNameTypeService germplasmNameTypeService;

	@Autowired
	NameTypeValidator nameTypeValidator;

	@Override
	public List<GermplasmNameTypeDTO> getNameTypes(final Pageable pageable) {
		return this.germplasmNameTypeService.getNameTypes(pageable);
	}

	@Override
	public long countNameTypes() {
		return this.germplasmNameTypeService.countNameTypes();
	}

	@Override
	public Integer createNameType(final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO) {
		this.nameTypeValidator.validate(germplasmNameTypeRequestDTO);
		return this.germplasmNameTypeService.createNameType(germplasmNameTypeRequestDTO);
	}
}
