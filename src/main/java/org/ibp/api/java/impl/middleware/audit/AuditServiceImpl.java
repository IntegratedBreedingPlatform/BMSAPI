package org.ibp.api.java.impl.middleware.audit;

import org.generationcp.middleware.service.impl.audit.GermplasmNameChangeDTO;
import org.ibp.api.java.audit.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {

	@Autowired
	private org.generationcp.middleware.service.api.audit.AuditService auditService;

	@Override
	public List<GermplasmNameChangeDTO> getNameChangesByNameId(final Integer nameId, final Pageable pageable) {
		return this.auditService.getNameChangesByNameId(nameId, pageable);
	}

	@Override
	public long countNameChangesByNameId(final Integer nameId) {
		return this.auditService.countNameChangesByNameId(nameId);
	}

}
