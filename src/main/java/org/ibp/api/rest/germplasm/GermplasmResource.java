package org.ibp.api.rest.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportResponseDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmInventoryImportDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmMatchRequestDto;
import org.ibp.api.Util;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.germplasm.GermplasmTemplateExportService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmImportRequestDtoValidator;
import org.ibp.api.java.inventory.manager.LotService;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
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
	private GermplasmImportRequestDtoValidator germplasmImportRequestDtoValidator;

	@ApiOperation(value = "Search germplasm. <b>Note:</b> Total count is not available for this query.")
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
					return germplasmService.countSearchGermplasm(germplasmSearchRequest, programUUID);
				}

				@Override
				public List<GermplasmSearchResponse> getResults(final PagedResult<GermplasmSearchResponse> pagedResult) {
					return germplasmService.searchGermplasm(germplasmSearchRequest, pageable, programUUID);
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

	@ApiOperation(value = "Search germplasm name types")
	@RequestMapping(value = "/crops/{cropName}/germplasm/name-types/search", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmNameTypeDTO>> searchNameTypes(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final String query) {
		return new ResponseEntity<>(this.germplasmService.searchNameTypes(query), HttpStatus.OK);
	}

	@ApiOperation(value = "Returns germplasm attributes filtered by a list of codes", notes = "Returns germplasm attributes filtered by a list of codes")
	@RequestMapping(value = "/crops/{cropName}/germplasm/attributes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<AttributeDTO>> getGermplasmAttributes(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final Set<String> codes) {

		return new ResponseEntity<>(this.germplasmService.filterGermplasmAttributes(codes), HttpStatus.OK);
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
	//FIXME: When removing current import germplasm, this preauthorize must be modified
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'GERMPLASM', 'IMPORT_GERMPLASM')")
	@RequestMapping(value = "/crops/{cropName}/germplasm", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<Integer, GermplasmImportResponseDto>> importGermplasm(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmImportRequestDto germplasmImportRequestDto) {
		return new ResponseEntity<>(this.germplasmService.importGermplasm(cropName, programUUID, germplasmImportRequestDto), HttpStatus.OK);
	}

	@ApiOperation(value = "Import germplasm updates. Updating Breeding Method is not yet supported.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'GERMPLASM', 'MANAGE_GERMPLASM', 'IMPORT_GERMPLASM_UPDATES')")
	@RequestMapping(value = "/crops/{cropName}/germplasm", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Set<Integer>> importGermplasmUpdates(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final List<GermplasmUpdateDTO> germplasmList) {
		return new ResponseEntity<>(this.germplasmService.importGermplasmUpdates(programUUID, germplasmList),
			HttpStatus.OK);
	}

	/**
	 * Returns a germplasm by a given germplasm id
	 *
	 * @return {@link GermplasmSearchResponse}
	 */
	@ApiOperation(value = "Returns a germplasm by a given germplasm id")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<GermplasmSearchResponse> getGermplasmById(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), LotService.class.getName());
		if (!Util.isPositiveInteger(String.valueOf(gid))) {
			errors.reject("gids.invalid", new String[] {gid.toString()}, "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGids(Arrays.asList(gid));
		germplasmSearchRequest.setAddedColumnsPropertyIds(Arrays.asList("PREFERRED NAME"));
		final List<GermplasmSearchResponse> germplasmSearchResponses =
			germplasmService.searchGermplasm(germplasmSearchRequest, null, programUUID);
		if (germplasmSearchResponses.isEmpty()) {
			errors.reject("gids.invalid", new String[] {gid.toString()}, "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
		return new ResponseEntity<>(germplasmSearchResponses.get(0), HttpStatus.OK);
	}

	@ApiOperation(value = "Validate the list of germplasm to be imported")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'GERMPLASM', 'IMPORT_GERMPLASM')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/validation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> validateImportGermplasmData(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final List<GermplasmInventoryImportDTO> germplasmDTOList) {
		germplasmImportRequestDtoValidator.validateImportLoadedData(programUUID, germplasmDTOList);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get a list of germplasm given a set of germplasmUUIDs and names")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'GERMPLASM', 'IMPORT_GERMPLASM')")
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
					return germplasmService.countSearchGermplasm(null, null);
				}

				@Override
				public long getFilteredCount() {
					//					return germplasmService.countGermplasmMatches(germplasmMatchRequestDto);
					// Not counting filtered germplasms for performance reasons
					return 0;
				}

				@Override
				public List<GermplasmDto> getResults(final PagedResult<GermplasmDto> pagedResult) {
					return germplasmService.findGermplasmMatches(germplasmMatchRequestDto, pageable);
				}
			});

		final List<GermplasmDto> pageResults = result.getPageResults();
		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(result.getTotalResults()));
		headers.add("X-Filtered-Count", Long.toString(result.getFilteredResults()));

		return new ResponseEntity<>(pageResults, headers, HttpStatus.OK);
	}

}
