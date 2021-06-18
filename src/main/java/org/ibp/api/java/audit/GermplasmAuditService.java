package org.ibp.api.java.audit;

import org.generationcp.middleware.service.impl.audit.GermplasmAttributeAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmNameAuditDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplasmAuditService {

	List<GermplasmNameAuditDTO> getNameChangesByNameId(Integer nameId, Pageable pageable);

	long countNameChangesByNameId(Integer nameId);

	List<GermplasmAttributeAuditDTO> getAttributeChangesByAttributeId(Integer attributeId, Pageable pageable);

	long countAttributeChangesByNameId(Integer attributeId);

}
