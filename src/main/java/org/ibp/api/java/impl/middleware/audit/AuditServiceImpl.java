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
	public List<GermplasmNameChangeDTO> getNameChangesByGidAndNameId(final Integer gid, final Integer nameId, final Pageable pageable) {
		return this.auditService.getNameChangesByGidAndNameId(gid, nameId, pageable);
	}

	@Override
	public long countNameChangesByGidAndNameId(final Integer gid, final Integer nameId) {
		return this.auditService.countNameChangesByGidAndNameId(gid, nameId);
	}

}
