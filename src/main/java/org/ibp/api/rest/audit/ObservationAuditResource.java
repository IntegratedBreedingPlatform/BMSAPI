package org.ibp.api.rest.audit;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.api.dataset.ObservationAuditDTO;
import org.ibp.api.java.audit.ObservationAuditService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api(value = "Observation Audit Resource")
@Controller
@RequestMapping("/crops")
public class ObservationAuditResource {

	@Autowired
	private ObservationAuditService observationAuditService;

	@ApiOperation(value = "Get Observation Audit", notes = "Get Observation Audit")
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/observationUnits/{observationUnitId}/variable/{variableId}/changes", method = RequestMethod.GET)
	public ResponseEntity<List<ObservationAuditDTO>> getPhenotypeAudit(
		@PathVariable final String crop, @PathVariable final String observationUnitId, @PathVariable final Integer variableId,
		final Pageable pageable) {

		final List<ObservationAuditDTO> phenotypeAuditList =
			this.observationAuditService.getObservationAuditList(observationUnitId, variableId, pageable);

		return new PaginatedSearch().getPagedResult(() ->
				this.observationAuditService.countObservationAudit(observationUnitId, variableId),
			() -> phenotypeAuditList,
			pageable);
	}

}
