
package org.ibp.api.brapi.v1.program;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v2.validation.CropValidator;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a> Location services.
 *
 * @author Diego Cuenya
 */
@Api(value = "BrAPI Program Services")
@Controller
public class ProgramResourceBrapi {

	@Autowired
	private ProgramService programService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private CropValidator cropValidator;

	@ApiOperation(value = "List Programs", notes = "Get a list of programs.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_PROGRAMS', 'MANAGE_PROGRAM_SETTINGS')")
	@RequestMapping(value = "/{crop}/brapi/v1/programs", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV1_3.class)
	public ResponseEntity<EntityListResponse<Program>> listPrograms(@PathVariable final String crop,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false) @RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false) @RequestParam(value = "pageSize",
			required = false) final Integer pageSize,
		@ApiParam(value = "Filter by program name. Exact match.", required = false) @RequestParam(value = "programName",
			required = false) final String programName,
		@ApiParam(value = "Filter by program abbreviation. Exact match.", required = false) @RequestParam(value = "abbreviation",
			required = false) final String abbreviation) {
		this.cropValidator.validateCrop(crop);

		if (!StringUtils.isBlank(abbreviation)) {
			final List<Map<String, String>> status =
				Collections.singletonList(ImmutableMap.of("message", "Abbreviation is not yet supported"));
			final Metadata metadata = new Metadata(null, status);
			return new ResponseEntity<>(new EntityListResponse<>(metadata, null),
				HttpStatus.NOT_IMPLEMENTED);
		}

		final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
		programSearchRequest.setProgramName(programName);
		programSearchRequest.setCommonCropName(crop);
		programSearchRequest.setAbbreviation(abbreviation);
		programSearchRequest.setLoggedInUserId(this.securityService.getCurrentlyLoggedInUser().getUserid());

		final PagedResult<ProgramDetailsDto> pagedResult =
			new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<ProgramDetailsDto>() {

				@Override
				public long getCount() {
					return ProgramResourceBrapi.this.programService.countProgramsByFilter(programSearchRequest);
				}

				@Override
				public List<ProgramDetailsDto> getResults(final PagedResult<ProgramDetailsDto> pagedResult) {
					final int pageNumber = pagedResult.getPageNumber();
					return ProgramResourceBrapi.this.programService
						.getProgramDetailsByFilter(new PageRequest(pageNumber, pagedResult.getPageSize()), programSearchRequest);
				}
			});

		return ProgramEntityResponseBuilder.getEntityListResponseResponseEntity(pagedResult);
	}
}
