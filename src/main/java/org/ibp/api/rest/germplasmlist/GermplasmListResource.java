package org.ibp.api.rest.germplasmlist;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasmlist.GermplasmListColumnDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListMeasurementVariableDTO;
import org.generationcp.middleware.api.germplasmlist.MyListsDTO;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchResponse;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataUpdateViewDTO;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListReorderEntriesRequest;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchRequest;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchResponse;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.search.SearchDto;
import org.ibp.api.java.germplasm.GermplasmListDataService;
import org.ibp.api.java.germplasm.GermplasmListService;
import org.ibp.api.java.germplasm.GermplasmListTemplateExportService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.germplasm.ReorderEntriesLock;
import org.ibp.api.java.impl.middleware.security.SecurityService;
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
import java.util.Set;

import static org.apache.commons.lang3.math.NumberUtils.isNumber;

@Api(value = "Germplasm List Services")
@Controller
public class GermplasmListResource {

	private static final String MANAGE_GERMPLASM_LISTS_PERMISSIONS = "'LISTS', 'MANAGE_GERMPLASM_LISTS'";
	private static final String MANAGE_GERMPLASM_PERMISSIONS = "'GERMPLASM', 'MANAGE_GERMPLASM'";

	@Autowired
	public GermplasmListService germplasmListService;

	@Autowired
	public GermplasmListDataService germplasmListDataService;

	@Autowired
	public GermplasmListTemplateExportService germplasmListTemplateExportService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private ReorderEntriesLock reorderEntriesLock;

	@ApiOperation(value = "Create a new Germplasm list")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists", method = RequestMethod.POST)
	// TODO add specific permission to create list from germplasm manager? IBP-5387
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_PERMISSIONS + ", 'IMPORT_GERMPLASM', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS
		+ ", 'IMPORT_GERMPLASM_LISTS')")
	@ResponseBody
	public ResponseEntity<GermplasmListGeneratorDTO> create(
		@ApiParam(required = true) @PathVariable final String crop,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmListGeneratorDTO request
	) {
		return new ResponseEntity<>(this.germplasmListService.create(request), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Import Germplasm list updates")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists", method = RequestMethod.PATCH)
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'IMPORT_GERMPLASM_LIST_UPDATES')")
	@ResponseBody
	public ResponseEntity<Void> importUpdates(
		@ApiParam(required = true) @PathVariable final String crop,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmListGeneratorDTO request
	) {
		// TODO see if we can use a subset of the fields of GermplasmListGeneratorDTO
		this.germplasmListService.importUpdates(request);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Get germplasm lists types", notes = "Get germplasm lists types")
	@RequestMapping(value = "/crops/{crop}/germplasm-list-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmListTypeDTO>> getGermplasmListTypes(
		@ApiParam(value = "The crop type", required = true) @PathVariable final String crop,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID) {
		final List<GermplasmListTypeDTO> germplasmListTypes = this.germplasmListService.getGermplasmListTypes();
		return new ResponseEntity<>(germplasmListTypes, HttpStatus.OK);
	}

	@ApiOperation(value = "Add germplasm entries to an existing list")
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_PERMISSIONS + ", 'MG_ADD_ENTRIES_TO_LIST', "
		+ MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'ADD_GERMPLASM_LIST_ENTRIES')")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/{germplasmListId}/entries", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> addGermplasmEntriesToList(
		@ApiParam(required = true) @PathVariable final String crop,
		@PathVariable final Integer germplasmListId,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final SearchCompositeDto<GermplasmSearchRequest, Integer> searchComposite
	) {
		this.germplasmListService.addGermplasmEntriesToList(germplasmListId, searchComposite, programUUID);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Import germplasm list entries from an existing list")
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'ADD_ENTRIES_TO_LIST')")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/{germplasmListId}/entries/import", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> addGermplasmListEntriesToAnotherList(
		@ApiParam(required = true) @PathVariable final String crop,
		@PathVariable final Integer germplasmListId,
		@RequestParam final Integer sourceGermplasmListId,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final SearchCompositeDto<GermplasmListDataSearchRequest, Integer> searchComposite
	) {
		this.germplasmListService.addGermplasmListEntriesToAnotherList(crop, programUUID, germplasmListId, sourceGermplasmListId,
			searchComposite);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Remove germplasm entries from an existing list")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/{germplasmListId}/entries", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'REMOVE_ENTRIES_GERMPLASM_LISTS')")
	public ResponseEntity<Void> removeGermplasmEntriesFromList(
		@ApiParam(required = true) @PathVariable final String crop,
		@PathVariable final Integer germplasmListId,
		@RequestParam(required = true) final Set<Integer> selectedEntries
	) {
		final SearchCompositeDto<GermplasmListDataSearchRequest, Integer> searchComposite = new SearchCompositeDto<>();
		searchComposite.setItemIds(selectedEntries);
		this.germplasmListService.removeGermplasmEntriesFromList(germplasmListId, searchComposite);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation("Get my lists")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/my-lists", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property,asc|desc. ")
	})
	@ResponseBody
	public ResponseEntity<List<MyListsDTO>> getMyLists(
		@PathVariable final String cropName,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable
	) {
		final Integer userId = this.securityService.getCurrentlyLoggedInUser().getUserid();
		final PagedResult<MyListsDTO> result =
			new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<MyListsDTO>() {

				@Override
				public long getCount() {
					return GermplasmListResource.this.germplasmListService.countMyLists(programUUID, userId);
				}

				@Override
				public List<MyListsDTO> getResults(final PagedResult<MyListsDTO> pagedResult) {
					return GermplasmListResource.this.germplasmListService.getMyLists(programUUID, pageable, userId);
				}
			});
		final List<MyListsDTO> pageResults = result.getPageResults();
		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(result.getTotalResults()));
		return new ResponseEntity<>(pageResults, headers, HttpStatus.OK);
	}

	@ApiOperation("Search germplasm lists")
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'SEARCH_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/search", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property,asc|desc. ")
	})
	@ResponseBody
	public ResponseEntity<List<GermplasmListSearchResponse>> searchGermplasmLists(
		@PathVariable final String cropName,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmListSearchRequest request,
		@ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable) {
		return new PaginatedSearch().getPagedResult(() -> this.germplasmListService.countSearchGermplasmList(request, programUUID),
			() -> this.germplasmListService.searchGermplasmList(request, pageable, programUUID),
			pageable);
	}

	@ApiOperation(value = "Post germplasm list data search", notes = "Post germplasm list data search")
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'SEARCH_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<SearchDto> postSearchGermplasmListData(
		@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmListDataSearchRequest request) {

		final String searchRequestId =
			this.searchRequestService.saveSearchRequest(request, GermplasmListDataSearchRequest.class).toString();
		return new ResponseEntity<>(new SearchDto(searchRequestId), HttpStatus.OK);
	}

	@ApiOperation(value = "Returns a germplasm list data by a given germplasm list id")
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'SEARCH_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/search", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property,asc|desc. ")
	})
	@ResponseBody
	public ResponseEntity<List<GermplasmListDataSearchResponse>> searchGermplasmListData(
		@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@RequestParam final Integer searchRequestId,
		@ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable) {

		final GermplasmListDataSearchRequest request = (GermplasmListDataSearchRequest) this.searchRequestService
			.getSearchRequest(searchRequestId, GermplasmListDataSearchRequest.class);

		return new PaginatedSearch().getPagedResult(() -> this.germplasmListDataService.countSearchGermplasmListData(listId, request),
			() -> this.germplasmListDataService.searchGermplasmListData(listId, request, pageable),
			pageable);
	}

	@ApiOperation(value = "Returns a list by a given list id")
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'SEARCH_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<GermplasmListDto> getGermplasmListById(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmListService.getGermplasmListById(listId), HttpStatus.OK);
	}

	@ApiIgnore
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/toggle-status", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Boolean> toggleGermplasmListStatus(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmListService.toggleGermplasmListStatus(listId), HttpStatus.OK);
	}

	@ApiIgnore
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/columns", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmListColumnDTO>> getGermplasmListColumns(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmListDataService.getGermplasmListColumns(listId, programUUID), HttpStatus.OK);
	}

	@ApiIgnore
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/table/columns", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmListMeasurementVariableDTO>> getGermplasmListDataTableHeader(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmListDataService.getGermplasmListDataTableHeader(listId, programUUID), HttpStatus.OK);
	}

	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/templates/xls/{isGermplasmListUpdateFormat}", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> getImportGermplasmExcelTemplate(@PathVariable final String cropName,
		@PathVariable final boolean isGermplasmListUpdateFormat,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final String fileNamePrefix) {

		final File file =
			this.germplasmListTemplateExportService.export(cropName, programUUID, isGermplasmListUpdateFormat, fileNamePrefix);

		final HttpHeaders headers = new HttpHeaders();
		headers
			.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		headers.add(HttpHeaders.CONTENT_TYPE, String.format("%s;charset=utf-8", FileUtils.detectMimeType(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	@ApiIgnore
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'SEARCH_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/view", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Void> updateGermplasmListDataView(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final List<GermplasmListDataUpdateViewDTO> view) {
		this.germplasmListDataService.updateGermplasmListDataView(listId, view);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation("Set generation level for list and fill with cross expansion")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'LISTS', 'GERMPLASM_LISTS', 'MANAGE_GERMPLASM_LISTS', 'SEARCH_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/pedigree-generation-level", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Void> fillWithCrossExpansion(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID,
		@RequestBody @ApiParam("a positive number, without quotation marks. E.g level: 2") final String level
	) {
		BaseValidator.checkArgument(isNumber(level), "error.generationlevel.invalid");
		final int levelInt = Integer.parseInt(level);
		BaseValidator.checkArgument(levelInt > 0 && levelInt <= 10 , "error.generationlevel.max");
		this.germplasmListDataService.fillWithCrossExpansion(listId, levelInt);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Reorder the selected entries to a given position or at the end of list.")
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'REORDER_ENTRIES_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/entries/reorder", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Void> reorderEntries(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmListReorderEntriesRequest request) {

		try {
			this.reorderEntriesLock.lockWrite();
			this.germplasmListDataService.reOrderEntries(listId, request);
		} finally {
			this.reorderEntriesLock.unlockWrite();
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Clone germplasm list")
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'CLONE_GERMPLASM_LIST')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/clone", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GermplasmListDto> cloneList(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmListDto request) {

		return new ResponseEntity<>(this.germplasmListService.clone(listId, request), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Edit List metatadata")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAnyAuthority('ADMIN', " + MANAGE_GERMPLASM_LISTS_PERMISSIONS + ", 'EDIT_LIST_METADATA')")
	@ResponseBody
	public ResponseEntity<Void> editListMetadata(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmListDto request
	) {
		request.setListId(listId);
		this.germplasmListService.editListMetadata(request, programUUID);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Delete germplasm list", notes = "Delete germplasm list.")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/{listId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteGermplasmList(
		@PathVariable final String crop,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID) {
		this.germplasmListService.deleteGermplasmList(crop, programUUID, listId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Delete a name type associated to germplasm list", notes = "Delete a name type associated to germplasm list")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/name-types/{nameTypeId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_CROP_SETTINGS')")
	@ResponseBody
	public ResponseEntity<Void> deleteGermplasmListNameTypes(@PathVariable final String crop,
		@RequestParam(required = false) final String programUUID, @PathVariable final Integer nameTypeId) {
		this.germplasmListDataService.deleteNameTypeFromGermplasmList(nameTypeId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
