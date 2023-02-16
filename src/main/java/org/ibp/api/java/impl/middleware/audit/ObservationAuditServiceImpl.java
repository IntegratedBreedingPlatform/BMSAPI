package org.ibp.api.java.impl.middleware.audit;

import org.generationcp.middleware.service.api.dataset.ObservationAuditDTO;
import org.ibp.api.java.audit.ObservationAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ObservationAuditServiceImpl implements ObservationAuditService {

	@Autowired
	private org.generationcp.middleware.service.api.audit.ObservationAuditService mwObservationAuditService;

	@Override
	public List<ObservationAuditDTO> getObservationAuditList(final String observationUnitId, final Integer variableId,
		final Pageable pageable) {
		return this.mwObservationAuditService.getObservationAuditList(observationUnitId, variableId, pageable);
	}

	@Override
	public long countObservationAudit(final String observationUnitId, final Integer variableId) {
		return this.mwObservationAuditService.countObservationAudit(observationUnitId, variableId);
	}
}
