package org.ibp.api.rest.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.GermplasmStudyDto;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmBasicDetailsDto;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.domain.germplasm.ProgenitorsDetailsDto;
import org.generationcp.middleware.domain.germplasm.ProgenitorsUpdateRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportResponseDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmInventoryImportDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmMatchRequestDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.germplasm.GermplasmDeleteResponse;
import org.ibp.api.domain.search.SearchDto;
import org.ibp.api.java.germplasm.GermplasmListService;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.germplasm.GermplasmTemplateExportService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmImportRequestDtoValidator;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.java.study.StudyService;
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
	private GermplasmTemplateExportService germplasmTemplateExportService;

	@Autowired
	private StudyService studyService;

	@Autowired
	private GermplasmImportRequestDtoValidator germplasmImportRequestDtoValidator;

	@Autowired
	private GermplasmListService germplasmListService;

	@Autowired
	private SampleService sampleService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private VariableService variableService;

	@ApiOperation(value = "Post germplasm search", notes = "Post germplasm search")
	@RequestMapping(value = "/crops/{cropName}/germplasm/search", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'SEARCH_GERMPLASM')" + HAS_GERMPLASM_SEARCH)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<SearchDto>> postSearchGermplasm(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmSearchRequest germplasmSearchRequest) {

		BaseValidator.checkNotNull(germplasmSearchRequest, "param.null", new String[] {"germplasmSearchDTO"});

		final String searchRequestId =
			this.searchRequestService.saveSearchRequest(germplasmSearchRequest, GermplasmSearchRequest.class).toString();
		final SearchDto searchDto = new SearchDto(searchRequestId);
		final SingleEntityResponse<SearchDto> singleEntityResponse = new SingleEntityResponse<SearchDto>(searchDto);

		return new ResponseEntity<>(singleEntityResponse, HttpStatus.OK);

	}

	@ApiOperation(value = "Search germplasm. <b>Note:</b> Total count is not available for this query.")
	@RequestMapping(value = "/crops/{cropName}/germplasm/search", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'SEARCH_GERMPLASM')" + HAS_GERMPLASM_SEARCH)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page. <b>Note:</b> this query may return additional records using some filters"),
		@ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property,asc|desc. ")
	})
	@ResponseBody
	public ResponseEntity<List<GermplasmSearchResponse>> getGermplasm(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final Integer searchRequestId,
		@ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable
	) {

		final GermplasmSearchRequest germplasmSearchRequest = (GermplasmSearchRequest) this.searchRequestService
			.getSearchRequest(searchRequestId, GermplasmSearchRequest.class);

		final PagedResult<GermplasmSearchResponse> result =
			new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<GermplasmSearchResponse>() {

				@Override
				public long getCount() {
					/**
					 * Excluding total count improves overall search from ~15s to 1s in wheat,brachiaria (~7M records)
					 * excluding deleted germplasm in query being the main bottleneck
					 */
					return 0;
				}

				@Override
				public long getFilteredCount() {
					return GermplasmResource.this.germplasmService.countSearchGermplasm(germplasmSearchRequest, programUUID);
				}

				@Override
				public List<GermplasmSearchResponse> getResults(final PagedResult<GermplasmSearchResponse> pagedResult) {
					return GermplasmResource.this.germplasmService.searchGermplasm(germplasmSearchRequest, pageable, programUUID);
				}
			});

		final List<GermplasmSearchResponse> pageResults = result.getPageResults();
		final HttpHeaders headers = new HttpHeaders();
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
	public ResponseEntity<List<Variable>> searchAttributes(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = true) final String query) {

		return new ResponseEntity<>(this.variableService.searchAttributeVariables(query, programUUID), HttpStatus.OK);
	}

	@ApiOperation(value = "Returns germplasm name types filtered by a list of codes", notes = "Returns germplasm name types filtered by a list of codes")
	@RequestMapping(value = "/crops/{cropName}/germplasm/name-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmNameTypeDTO>> getGermplasmNameTypes(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final Set<String> codes) {

		return new ResponseEntity<>(this.germplasmService.filterGermplasmNameTypes(codes), HttpStatus.OK);
	}

	@ApiOperation(value = "Search germplasm name types")
	@RequestMapping(value = "/crops/{cropName}/germplasm/name-types/search", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmNameTypeDTO>> searchNameTypes(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final String query) {
		return new ResponseEntity<>(this.germplasmService.searchNameTypes(query), HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropName}/germplasm/templates/xls/{isGermplasmUpdateFormat}", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> getImportGermplasmExcelTemplate(@PathVariable final String cropName,
		@PathVariable final boolean isGermplasmUpdateFormat,
		@RequestParam(required = false) final String programUUID) {

		final File file =
			this.germplasmTemplateExportService.export(cropName, programUUID, isGermplasmUpdateFormat);

		final HttpHeaders headers = new HttpHeaders();
		headers
			.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		headers.add(HttpHeaders.CONTENT_TYPE, String.format("%s;charset=utf-8", FileUtils.detectMimeType(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	/**
	 * Import a set of germplasm
	 *
	 * @return a map indicating the GID that was created per clientId, if null, no germplasm was created
	 */
	@ApiOperation(value = "Import a list of germplasm with pedigree information", notes = "connectUsing = NONE if any progenitors are specified. Otherwise use GID or GUID ")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'IMPORT_GERMPLASM')")
	@RequestMapping(value = "/crops/{cropName}/germplasm", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<Integer, GermplasmImportResponseDto>> importGermplasm(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmImportRequestDto germplasmImportRequestDto) {
		return new ResponseEntity<>(this.germplasmService.importGermplasm(cropName, programUUID, germplasmImportRequestDto), HttpStatus.OK);
	}

	@ApiOperation(value = "Import germplasm updates. Updating Breeding Method is not yet supported.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'IMPORT_GERMPLASM_UPDATES')")
	@RequestMapping(value = "/crops/{cropName}/germplasm", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Set<Integer>> importGermplasmUpdates(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final List<GermplasmUpdateDTO> germplasmList) {
		return new ResponseEntity<>(this.germplasmService.importGermplasmUpdates(programUUID, germplasmList),
			HttpStatus.OK);
	}

	@ApiOperation(value = "Validate the list of germplasm to be imported")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'IMPORT_GERMPLASM')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/validation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> validateImportGermplasmData(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final List<GermplasmInventoryImportDTO> germplasmDTOList) {
		this.germplasmImportRequestDtoValidator.validateImportLoadedData(programUUID, germplasmDTOList);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get a list of germplasm given a set of germplasmUUIDs and names")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'IMPORT_GERMPLASM')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/matches", method = RequestMethod.POST)
	@ResponseBody
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page. <b>Note:</b> this query may return additional records using some filters")
	})
	public ResponseEntity<List<GermplasmDto>> getGermplasmMatches(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmMatchRequestDto germplasmMatchRequestDto,
		@ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable) {

		final PagedResult<GermplasmDto> result =
			new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<GermplasmDto>() {

				@Override
				public long getCount() {
					return GermplasmResource.this.germplasmService.countSearchGermplasm(null, null);
				}

				@Override
				public long getFilteredCount() {
					//					return germplasmService.countGermplasmMatches(germplasmMatchRequestDto);
					// Not counting filtered germplasms for performance reasons
					return 0;
				}

				@Override
				public List<GermplasmDto> getResults(final PagedResult<GermplasmDto> pagedResult) {
					return GermplasmResource.this.germplasmService.findGermplasmMatches(germplasmMatchRequestDto, pageable);
				}
			});

		final List<GermplasmDto> pageResults = result.getPageResults();
		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(result.getTotalResults()));
		headers.add("X-Filtered-Count", Long.toString(result.getFilteredResults()));

		return new ResponseEntity<>(pageResults, headers, HttpStatus.OK);
	}

	@ApiOperation(value = "Delete germplasm")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'DELETE_GERMPLASM')")
	@RequestMapping(value = "/crops/{cropName}/germplasm", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<GermplasmDeleteResponse> deleteGermplasm(@PathVariable final String cropName,
		@RequestParam final List<Integer> gids) {
		return new ResponseEntity<>(this.germplasmService.deleteGermplasm(gids), HttpStatus.OK);
	}

	/**
	 * Returns the studies of the given germplasm
	 *
	 * @return {@link GermplasmSearchResponse}
	 */
	@ApiOperation(value = "Returns the studies of the given germplasm")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/studies", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmStudyDto>> getGermplasmStudies(
		@PathVariable final String cropName, @PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.studyService.getGermplasmStudies(gid), HttpStatus.OK);
	}

	@ApiOperation(value = "Get lists of specified germplasm")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/lists", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmListDto>> getGermplasmLists(@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmListService.getGermplasmLists(gid), HttpStatus.OK);
	}

	/**
	 * Returns a germplasm by a given germplasm id
	 *
	 * @return {@link GermplasmDto}
	 */
	@ApiOperation(value = "Returns a germplasm by a given germplasm id")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<GermplasmDto> getGermplasmDtoById(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmService.getGermplasmDtoById(gid), HttpStatus.OK);
	}

	@ApiOperation(value = "Get samples of specified germplasm")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/samples", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<SampleDTO>> getGermplasmSamples(@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.sampleService.getGermplasmSamples(gid), HttpStatus.OK);
	}

	@ApiOperation(value = "Get progenitor details of specified germplasm")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/progenitor-details", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ProgenitorsDetailsDto> getGermplasmProgenitorDetails(@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmService.getGermplasmProgenitorDetails(gid), HttpStatus.OK);
	}

	/**
	 * Modify germplasm basic details
	 *
	 * @return {@link GermplasmDto}
	 */
	@ApiOperation(value = "Update germplasm basic details")
	@PreAuthorize("hasAnyAuthority('ADMIN','GERMPLASM', 'MANAGE_GERMPLASM', 'EDIT_GERMPLASM', 'MODIFY_BASIC_DETAILS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/basic-details", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Void> updateGermplasmBasicDetails(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmBasicDetailsDto germplasmBasicDetailsDto) {
		final boolean updateExecuted = this.germplasmService.updateGermplasmBasicDetails(programUUID, gid, germplasmBasicDetailsDto);
		return new ResponseEntity<>((updateExecuted) ? HttpStatus.OK : HttpStatus.NO_CONTENT);
	}

	/**
	 * Modify germplasm pedigree
	 *
	 * @return {@link GermplasmDto}
	 */
	@ApiOperation(value = "Update germplasm pedigree: breeding method and progenitors")
	@PreAuthorize("hasAnyAuthority('ADMIN','GERMPLASM', 'MANAGE_GERMPLASM', 'EDIT_GERMPLASM', 'MODIFY_PEDIGREE')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/progenitor-details", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Void> updateGermplasmPedigree(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto) {
		final boolean updateExecuted = this.germplasmService.updateGermplasmPedigree(programUUID, gid, progenitorsUpdateRequestDto);
		return new ResponseEntity<>((updateExecuted) ? HttpStatus.OK : HttpStatus.NO_CONTENT);
	}

}
