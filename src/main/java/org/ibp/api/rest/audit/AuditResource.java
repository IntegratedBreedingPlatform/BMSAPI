package org.ibp.api.rest.audit;

import org.generationcp.middleware.service.impl.audit.GermplasmNameChangeDTO;
import org.ibp.api.java.audit.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AuditResource {

	@Autowired
	private AuditService auditService;

	@RequestMapping(
		value = "/crops/{cropName}/germplasm/{gid}/name/{nameId}/changes",
		method = RequestMethod.GET)
	public ResponseEntity<List<GermplasmNameChangeDTO>> getNameChanges(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@PathVariable final Integer nameId) {
		final List<GermplasmNameChangeDTO> namesAuditByNameId = this.auditService.getNameChangesByGidAndNameId(gid, nameId);
		return new ResponseEntity<>(namesAuditByNameId, HttpStatus.OK);
	}

}
