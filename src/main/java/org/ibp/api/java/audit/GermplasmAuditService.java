package org.ibp.api.java.audit;

import org.generationcp.middleware.service.impl.audit.GermplasmAttributeAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmBasicDetailsAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmNameAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmReferenceAuditDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplasmAuditService {

	List<GermplasmNameAuditDTO> getNameChangesByNameId(Integer nameId, Pageable pageable);

	long countNameChangesByNameId(Integer nameId);

	List<GermplasmAttributeAuditDTO> getAttributeChangesByAttributeId(Integer attributeId, Pageable pageable);

	long countAttributeChangesByNameId(Integer attributeId);

	List<GermplasmBasicDetailsAuditDTO> getBasicDetailsChangesByGid(Integer gid, Pageable pageable);

	long countBasicDetailsChangesByGid(Integer gid);

	List<GermplasmReferenceAuditDTO> getReferenceChangesByGid(Integer gid, Pageable pageable);

	long countReferenceChangesByGid(Integer gid);

}
