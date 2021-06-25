package org.ibp.api.java.impl.middleware.audit;

import org.generationcp.middleware.service.impl.audit.GermplasmAttributeAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmBasicDetailsAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmNameAuditDTO;
import org.ibp.api.java.audit.GermplasmAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GermplasmAuditServiceImpl implements GermplasmAuditService {

	@Autowired
	private org.generationcp.middleware.service.api.audit.GermplasmAuditService germplasmAuditService;

	@Override
	public List<GermplasmNameAuditDTO> getNameChangesByNameId(final Integer nameId, final Pageable pageable) {
		return this.germplasmAuditService.getNameChangesByNameId(nameId, pageable);
	}

	@Override
	public long countNameChangesByNameId(final Integer nameId) {
		return this.germplasmAuditService.countNameChangesByNameId(nameId);
	}

	@Override
	public List<GermplasmAttributeAuditDTO> getAttributeChangesByAttributeId(final Integer attributeId, final Pageable pageable) {
		return this.germplasmAuditService.getAttributeChangesByAttributeId(attributeId, pageable);
	}

	@Override
	public long countAttributeChangesByNameId(final Integer attributeId) {
		return this.germplasmAuditService.countAttributeChangesByAttributeId(attributeId);
	}

	@Override
	public List<GermplasmBasicDetailsAuditDTO> getBasicDetailsChangesByGid(final Integer gid, final Pageable pageable) {
		return this.germplasmAuditService.getBasicDetailsChangesByGid(gid, pageable);
	}

	@Override
	public long countBasicDetailsChangesByGid(final Integer gid) {
		return this.germplasmAuditService.countBasicDetailsChangesByGid(gid);
	}

}
