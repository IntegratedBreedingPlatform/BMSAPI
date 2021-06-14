package org.ibp.api.rest.audit;

import org.generationcp.middleware.service.impl.audit.GermplasmNameChangeDTO;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.audit.AuditService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AuditResource {

	private static final String HAS_VIEW_CHANGE_HISTORY = "hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'VIEW_CHANGE_HISTORY')";

	@Autowired
	private AuditService auditService;

	@ResponseBody
	@RequestMapping(
		value = "/crops/{cropName}/germplasm/{gid}/name/{nameId}/changes",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_VIEW_CHANGE_HISTORY)
	public ResponseEntity<List<GermplasmNameChangeDTO>> getNameChanges(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@PathVariable final Integer nameId,
		final Pageable pageable) {

		final PagedResult<GermplasmNameChangeDTO> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<GermplasmNameChangeDTO>() {

				@Override
				public long getCount() {
					return AuditResource.this.auditService.countNameChangesByGidAndNameId(gid, nameId);
				}

				@Override
				public List<GermplasmNameChangeDTO> getResults(final PagedResult<GermplasmNameChangeDTO> pagedResult) {
					return AuditResource.this.auditService.getNameChangesByGidAndNameId(gid, nameId, pageable);
				}
			});


		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));
		return new ResponseEntity<>(resultPage.getPageResults(), headers, HttpStatus.OK);
	}

}
