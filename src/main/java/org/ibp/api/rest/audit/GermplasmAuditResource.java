package org.ibp.api.rest.audit;

import org.generationcp.middleware.service.impl.audit.GermplasmAttributeAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmBasicDetailsAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmNameAuditDTO;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.audit.GermplasmAuditService;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmNameRequestValidator;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
public class GermplasmAuditResource {

	private static final String HAS_VIEW_CHANGE_HISTORY = "hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'VIEW_CHANGE_HISTORY')";

	@Autowired
	private GermplasmAuditService auditService;

	@Autowired
	private GermplasmNameRequestValidator germplasmNameRequestValidator;

	@Autowired
	private AttributeValidator attributeValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@ResponseBody
	@RequestMapping(
		value = "/crops/{cropName}/germplasm/{gid}/name/{nameId}/changes",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_VIEW_CHANGE_HISTORY)
	public ResponseEntity<List<GermplasmNameAuditDTO>> getNameChanges(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@PathVariable final Integer nameId,
		final Pageable pageable) {

		this.germplasmNameRequestValidator.validateNameBelongsToGermplasm(gid, nameId);

		final PagedResult<GermplasmNameAuditDTO> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<GermplasmNameAuditDTO>() {

				@Override
				public long getCount() {
					return GermplasmAuditResource.this.auditService.countNameChangesByNameId(nameId);
				}

				@Override
				public List<GermplasmNameAuditDTO> getResults(final PagedResult<GermplasmNameAuditDTO> pagedResult) {
					return GermplasmAuditResource.this.auditService.getNameChangesByNameId(nameId, pageable);
				}
			});


		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));
		return new ResponseEntity<>(resultPage.getPageResults(), headers, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(
		value = "/crops/{cropName}/germplasm/{gid}/attribute/{attributeId}/changes",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_VIEW_CHANGE_HISTORY)
	public ResponseEntity<List<GermplasmAttributeAuditDTO>> getAttributeChanges(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@PathVariable final Integer attributeId,
		final Pageable pageable) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.attributeValidator.validateGermplasmAttributeExists(errors, gid, attributeId);

		final PagedResult<GermplasmAttributeAuditDTO> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<GermplasmAttributeAuditDTO>() {

				@Override
				public long getCount() {
					return GermplasmAuditResource.this.auditService.countAttributeChangesByNameId(attributeId);
				}

				@Override
				public List<GermplasmAttributeAuditDTO> getResults(final PagedResult<GermplasmAttributeAuditDTO> pagedResult) {
					return GermplasmAuditResource.this.auditService.getAttributeChangesByAttributeId(attributeId, pageable);
				}
			});


		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));
		return new ResponseEntity<>(resultPage.getPageResults(), headers, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(
		value = "/crops/{cropName}/germplasm/{gid}/basic-details/changes",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_VIEW_CHANGE_HISTORY)
	public ResponseEntity<List<GermplasmBasicDetailsAuditDTO>> getBasicDetailsChanges(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		final Pageable pageable) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGermplasmId(errors, gid);

		final PagedResult<GermplasmBasicDetailsAuditDTO> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<GermplasmBasicDetailsAuditDTO>() {

				@Override
				public long getCount() {
					return GermplasmAuditResource.this.auditService.countBasicDetailsChangesByGid(gid);
				}

				@Override
				public List<GermplasmBasicDetailsAuditDTO> getResults(final PagedResult<GermplasmBasicDetailsAuditDTO> pagedResult) {
					return GermplasmAuditResource.this.auditService.getBasicDetailsChangesByGid(gid, pageable);
				}
			});


		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));
		return new ResponseEntity<>(resultPage.getPageResults(), headers, HttpStatus.OK);
	}

}
