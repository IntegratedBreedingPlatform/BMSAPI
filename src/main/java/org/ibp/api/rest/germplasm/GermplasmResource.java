package org.ibp.api.rest.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.api.germplasm.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmImportResponseDto;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.germplasm.GermplasmTemplateExportService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Api(value = "Germplasm Services")
@Controller
public class GermplasmResource {

	private static final String HAS_GERMPLASM_SEARCH = " or hasAnyAuthority('STUDIES'"
		+ ", 'MANAGE_STUDIES'"
		+ ", 'QUERIES'"
		+ ", 'GRAPHICAL_QUERIES'"
		+ ")";

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private UserService userService;

	@Autowired
	private GermplasmTemplateExportService germplasmTemplateExportService;

	@ApiOperation(value = "Search germplasm")
	@RequestMapping(value = "/crops/{cropName}/germplasm/search", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'GERMPLASM', 'MANAGE_GERMPLASM', 'SEARCH_GERMPLASM')" + HAS_GERMPLASM_SEARCH)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page. <b>Note:</b> this query may return additional records using some filters"),
		@ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property,asc|desc. ")
	})
	@ResponseBody
	public ResponseEntity<List<GermplasmSearchResponse>> searchGermplasm(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmSearchRequest germplasmSearchRequest,
		@ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable
	) {

		BaseValidator.checkNotNull(germplasmSearchRequest, "param.null", new String[] {"germplasmSearchDTO"});

		final String userName = SecurityUtil.getLoggedInUserName();
		final WorkbenchUser user = this.userService.getUserWithAuthorities(userName, cropName, programUUID);

		// TODO Move to dao method, so it cannot be bypassed? IBP-4166
		if (user.hasOnlyProgramRoles(cropName)) {
			germplasmSearchRequest.setInProgramListOnly(true);
		}

		final PagedResult<GermplasmSearchResponse> result =
			new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<GermplasmSearchResponse>() {

				@Override
				public long getCount() {
					return germplasmService.countSearchGermplasm(null, programUUID);
				}

				@Override
				public long getFilteredCount() {
					return germplasmService.countSearchGermplasm(germplasmSearchRequest, programUUID);
				}

				@Override
				public List<GermplasmSearchResponse> getResults(final PagedResult<GermplasmSearchResponse> pagedResult) {
					return germplasmService.searchGermplasm(germplasmSearchRequest, pageable, programUUID);
				}
			});

		final List<GermplasmSearchResponse> pageResults = result.getPageResults();
		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(result.getTotalResults()));
		headers.add("X-Filtered-Count", Long.toString(result.getFilteredResults()));

		return new ResponseEntity<>(pageResults, headers, HttpStatus.OK);
	}

	/**
	 * Simple search to feed autocomplete features
	 *
	 * @return a limited set of results matching the query criteria
	 */
	@ApiOperation(value = "Search germplasm attributes")
	@RequestMapping(value = "/crops/{cropName}/germplasm/attributes/search", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<AttributeDTO>> searchAttributes(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = true) final String query) {

		return new ResponseEntity<>(this.germplasmService.searchAttributes(query), HttpStatus.OK);
	}

	@ApiOperation(value = "Returns germplasm name types filtered by a list of codes", notes = "Returns germplasm name types filtered by a list of codes")
	@RequestMapping(value = "/crops/{cropName}/germplasm/name-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmNameTypeDTO>> getGermplasmNameTypes(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final Set<String> codes) {

		return new ResponseEntity<>(this.germplasmService.filterGermplasmNameTypes(codes), HttpStatus.OK);
	}

	@ApiOperation(value = "Returns germplasm attributes filtered by a list of codes", notes = "Returns germplasm attributes filtered by a list of codes")
	@RequestMapping(value = "/crops/{cropName}/germplasm/attributes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<AttributeDTO>> getGermplasmAttributes(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final Set<String> codes) {

		return new ResponseEntity<>(this.germplasmService.filterGermplasmAttributes(codes), HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropName}/germplasm/templates/xls", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> getImportGermplasmExcelTemplate(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID) {

		final File file =
			this.germplasmTemplateExportService.export(cropName, programUUID);

		final HttpHeaders headers = new HttpHeaders();
		headers
			.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}
	/**
	 * Import a set of germplasm
	 *
	 * @return a map indicating the GID that was created per clientId, if null, no germplasm was created
	 */
	@ApiOperation(value = "Save a set of germplasm")
	//FIXME: When removing current import germplasm, this preauthorize must be modified
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'GERMPLASM', 'IMPORT_GERMPLASM')")
	@RequestMapping(value = "/crops/{cropName}/germplasm", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<Integer, GermplasmImportResponseDto>> importGermplasm(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmImportRequestDto germplasmImportRequestDto) {

		return new ResponseEntity<>(this.germplasmService.importGermplasm(cropName, programUUID, germplasmImportRequestDto), HttpStatus.OK);
	}

	@ApiOperation(value = "Import germplasm updates")
	@RequestMapping(value = "/crops/{cropName}/germplasm", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Void> importGermplasmUpdates(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		this.germplasmService.importGermplasmUpdates(programUUID, germplasmUpdateDTOList);

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
