package org.ibp.api.java.audit;

import org.generationcp.middleware.service.impl.audit.GermplasmNameChangeDTO;

import java.util.List;

public interface AuditService {

	List<GermplasmNameChangeDTO> getNameChangesByGidAndNameId(final Integer gid, Integer nameId);

}
