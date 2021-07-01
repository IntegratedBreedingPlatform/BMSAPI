package org.ibp.api.rest.audit;

import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.service.impl.audit.GermplasmAttributeAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmBasicDetailsAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmNameAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmOtherProgenitorsAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmProgenitorDetailsAuditDTO;
import org.generationcp.middleware.service.impl.audit.GermplasmReferenceAuditDTO;
import org.ibp.api.java.audit.GermplasmAuditService;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmNameRequestValidator;
import org.ibp.api.rest.common.PaginatedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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

	private static final String HAS_VIEW_GERMPLASM_CHANGE_HISTORY =
		"hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'VIEW_GERMPLASM_CHANGE_HISTORY')";

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
		value = "/crops/{cropName}/germplasm/{gid}/names/{nameId}/changes",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_VIEW_GERMPLASM_CHANGE_HISTORY)
	public ResponseEntity<List<GermplasmNameAuditDTO>> getNameChanges(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@PathVariable final Integer nameId,
		final Pageable pageable) {

		final MapBindingResult errors =
			new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
		this.germplasmValidator.validateGermplasmId(errors, gid);
		this.germplasmNameRequestValidator.validateNameBelongsToGermplasm(gid, nameId);

		return new PaginatedSearch().getPagedResult(() -> this.auditService.countNameChangesByNameId(nameId),
			() -> this.auditService.getNameChangesByNameId(nameId, pageable),
			pageable);
	}

	@ResponseBody
	@RequestMapping(
		value = "/crops/{cropName}/germplasm/{gid}/attributes/{attributeId}/changes",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_VIEW_GERMPLASM_CHANGE_HISTORY)
	public ResponseEntity<List<GermplasmAttributeAuditDTO>> getAttributeChanges(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@PathVariable final Integer attributeId,
		final Pageable pageable) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.attributeValidator.validateGermplasmAttributeExists(errors, gid, attributeId);

		return new PaginatedSearch().getPagedResult(() -> this.auditService.countAttributeChangesByNameId(attributeId),
			() -> this.auditService.getAttributeChangesByAttributeId(attributeId, pageable),
			pageable);
	}

	@ResponseBody
	@RequestMapping(
		value = "/crops/{cropName}/germplasm/{gid}/basic-details/changes",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_VIEW_GERMPLASM_CHANGE_HISTORY)
	public ResponseEntity<List<GermplasmBasicDetailsAuditDTO>> getBasicDetailsChanges(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		final Pageable pageable) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGermplasmId(errors, gid);

		return new PaginatedSearch().getPagedResult(() -> this.auditService.countBasicDetailsChangesByGid(gid),
			() -> this.auditService.getBasicDetailsChangesByGid(gid, pageable),
			pageable);
	}

	@ResponseBody
	@RequestMapping(
		value = "/crops/{cropName}/germplasm/{gid}/references/changes",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_VIEW_GERMPLASM_CHANGE_HISTORY)
	public ResponseEntity<List<GermplasmReferenceAuditDTO>> getReferenceChanges(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		final Pageable pageable) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGermplasmId(errors, gid);

		return new PaginatedSearch().getPagedResult(() -> this.auditService.countReferenceChangesByGid(gid),
			() -> this.auditService.getReferenceChangesByGid(gid, pageable),
			pageable);
	}

	@ResponseBody
	@RequestMapping(
		value = "/crops/{cropName}/germplasm/{gid}/progenitor-details/changes",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_VIEW_GERMPLASM_CHANGE_HISTORY)
	public ResponseEntity<List<GermplasmProgenitorDetailsAuditDTO>> getProgenitorDetailsChanges(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		final Pageable pageable) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGermplasmId(errors, gid);

		return new PaginatedSearch().getPagedResult(() -> this.auditService.countProgenitorDetailsChangesByGid(gid),
			() -> this.auditService.getProgenitorDetailsChangesByGid(gid, pageable),
			pageable);
	}

	@ResponseBody
	@RequestMapping(
		value = "/crops/{cropName}/germplasm/{gid}/other-progenitors/changes",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_VIEW_GERMPLASM_CHANGE_HISTORY)
	public ResponseEntity<List<GermplasmOtherProgenitorsAuditDTO>> getOtherProgenitorsChanges(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		final Pageable pageable) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGermplasmId(errors, gid);

		return new PaginatedSearch().getPagedResult(() -> this.auditService.countOtherProgenitorsChangesByGid(gid),
			() -> this.auditService.getOtherProgenitorsByGid(gid, pageable),
			pageable);
	}

}
