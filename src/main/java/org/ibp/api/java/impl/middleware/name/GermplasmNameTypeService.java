package org.ibp.api.java.impl.middleware.name;

import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplasmNameTypeService {


	List<GermplasmNameTypeDTO> getNameTypes(Pageable pageable);

	long countNameTypes();

	Integer createNameType(GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO);

}
