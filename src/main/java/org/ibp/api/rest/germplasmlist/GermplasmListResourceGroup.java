package org.ibp.api.rest.germplasmlist;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasmlist.GermplasmListColumnDTO;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataUpdateViewDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListMeasurementVariableDTO;
import org.generationcp.middleware.api.germplasmlist.MyListsDTO;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchResponse;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchRequest;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchResponse;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.germplasm.GermplasmListDataService;
import org.ibp.api.java.germplasm.GermplasmListService;
import org.ibp.api.java.germplasm.GermplasmListTemplateExportService;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.ibp.api.rest.common.UserTreeState;
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

@Api(value = "Germplasm List Services")
@Controller
public class GermplasmListResourceGroup {

	@Autowired
	public GermplasmListService germplasmListService;

	@Autowired
	public GermplasmListDataService germplasmListDataService;

	@Autowired
	public GermplasmListTemplateExportService germplasmListTemplateExportService;

	@Autowired
	private SecurityService securityService;

	@ApiOperation(value = "Get germplasm lists given a tree parent node folder", notes = "Get germplasm lists given a tree parent node folder")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'SEARCH_GERMPLASM')" + PermissionsEnum.HAS_INVENTORY_VIEW)
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/tree", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<TreeNode>> getGermplasmListByParentFolderId(
		@ApiParam(value = "The crop type", required = true) @PathVariable final String crop,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@ApiParam(value = "The id of the parent folder") @RequestParam(required = false) final String parentFolderId,
		@ApiParam(value = "Only folders") @RequestParam(required = true) final Boolean onlyFolders) {
		final List<TreeNode> children = this.germplasmListService.getGermplasmListChildrenNodes(crop, programUUID, parentFolderId, onlyFolders);
		return new ResponseEntity<>(children, HttpStatus.OK);
	}

	@ApiOperation(value = "Create a new Germplasm list")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists", method = RequestMethod.POST)
	// TODO The Permissions will be change after implement IBP-4570 (New list manager)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'IMPORT_GERMPLASM', 'LISTS', 'GERMPLASM_LISTS')")
	@ResponseBody
	public ResponseEntity<GermplasmListGeneratorDTO> create(
		@ApiParam(required = true) @PathVariable final String crop,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmListGeneratorDTO request
	) {
		return new ResponseEntity<>(this.germplasmListService.create(request), HttpStatus.CREATED);
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
					return germplasmListService.countMyLists(programUUID, userId);
				}

				@Override
				public List<MyListsDTO> getResults(final PagedResult<MyListsDTO> pagedResult) {
					return germplasmListService.getMyLists(programUUID, pageable, userId);
				}
			});
		final List<MyListsDTO> pageResults = result.getPageResults();
		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(result.getTotalResults()));
		return new ResponseEntity<>(pageResults, headers, HttpStatus.OK);
	}

	@ApiOperation(value = "Create germplasm list folder", notes = "Create sample list folder.")
 	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'SEARCH_GERMPLASM')")
	@RequestMapping(value = "/crops/{crop}/germplasm-list-folders", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity createGermplasmListFolder(
		@PathVariable final String crop,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final String folderName,
		@RequestParam final String parentId) {

		final Integer folderId = this.germplasmListService.createGermplasmListFolder(crop, programUUID, folderName, parentId);
		return new ResponseEntity<>(folderId, HttpStatus.OK);
	}

	@ApiOperation(value = "Update germplasm list folder", notes = "Update germplasm list folder.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'SEARCH_GERMPLASM')")
	@RequestMapping(value = "/crops/{crop}/germplasm-list-folders/{folderId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateGermplasmListFolderName(
		@PathVariable final String crop,
		@PathVariable final String folderId,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final String newFolderName) {

		final Integer updatedFolderId = this.germplasmListService.updateGermplasmListFolderName(crop, programUUID, newFolderName, folderId);
		return new ResponseEntity<>(updatedFolderId, HttpStatus.OK);
	}

	@ApiOperation(value = "Move germplasm list folder.", notes = "Move germplasm list folder.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'SEARCH_GERMPLASM')")
	@RequestMapping(value = "/crops/{crop}/germplasm-list-folders/{folderId}/move", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity moveGermplasmList(
		@PathVariable final String crop,
		@PathVariable final String folderId,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final String newParentId) {

		final Integer movedFolderId = this.germplasmListService.moveGermplasmListFolder(crop, programUUID, folderId, newParentId);
		return new ResponseEntity<>(movedFolderId, HttpStatus.OK);
	}

	@ApiOperation(value = "Delete germplasm list folder", notes = "Delete germplasm list folder.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'SEARCH_GERMPLASM')")
	@RequestMapping(value = "/crops/{crop}/germplasm-list-folders/{folderId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteGermplasmListFolder(
		@PathVariable final String crop,
		@PathVariable final String folderId,
		@RequestParam(required = false) final String programUUID) {
		this.germplasmListService.deleteGermplasmListFolder(crop, programUUID, folderId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get tree of expanded germplasm list folders last used by user", notes = "Get tree of expanded germplasm list folders last used by user")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'SEARCH_GERMPLASM')" + PermissionsEnum.HAS_INVENTORY_VIEW)
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/tree-state", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<TreeNode>> getUserTreeState(
		@ApiParam(value = "The crop type", required = true) @PathVariable final String crop,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@ApiParam(value = "The User ID") @RequestParam(required = true) final String userId) {
		return new ResponseEntity<>( this.germplasmListService.getUserTreeState(crop, programUUID, userId), HttpStatus.OK);
	}

	@ApiOperation(value = "Save hierarchy of germplasm list folders last used by user", notes = "Save hierarchy of germplasm list folders last used by user")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'SEARCH_GERMPLASM')" + PermissionsEnum.HAS_INVENTORY_VIEW)
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/tree-state", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> saveUserTreeState(
		@ApiParam(value = "The crop type", required = true) @PathVariable final String crop,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@RequestBody final UserTreeState treeState) {
		this.germplasmListService.saveGermplasmListTreeState(crop, programUUID, treeState);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation("Search germplasm lists")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'LISTS', 'GERMPLASM_LISTS', 'MANAGE_GERMPLASM_LISTS', 'SEARCH_GERMPLASM_LISTS')")
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

	@ApiOperation(value = "Returns a germplasm list data by a given germplasm list id")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'LISTS', 'GERMPLASM_LISTS', 'MANAGE_GERMPLASM_LISTS', 'SEARCH_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/data/search", method = RequestMethod.POST)
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
		@RequestBody final GermplasmListDataSearchRequest request,
		@ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable) {
		return new PaginatedSearch().getPagedResult(() -> this.germplasmListDataService.countSearchGermplasmListData(listId, request),
			() -> this.germplasmListDataService.searchGermplasmListData(listId, request, pageable),
			pageable);
	}

	@ApiOperation(value = "Returns a list by a given list id")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<GermplasmListDto> getGermplasmListById(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmListService.getGermplasmListById(listId), HttpStatus.OK);
	}

	//TODO: check permission
	@ApiIgnore
	@ApiOperation(value = "Locks or unlocks the list depending the current status of it")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/toggle-status", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Boolean> toggleGermplasmListStatus(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmListService.toggleGermplasmListStatus(listId), HttpStatus.OK);
	}

	@ApiIgnore
	@ApiOperation(value = "Get a list of static, names and germplasm descriptors columns for a given list")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/columns", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmListColumnDTO>> getGermplasmListColumns(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmListDataService.getGermplasmListColumns(listId, programUUID), HttpStatus.OK);
	}

	@ApiIgnore
	@ApiOperation(value = "Get the header for list table")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/table/columns", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmListMeasurementVariableDTO>> getGermplasmListDataTableHeader(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmListDataService.getGermplasmListDataTableHeader(listId, programUUID), HttpStatus.OK);
	}


	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/templates/xls", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> getImportGermplasmExcelTemplate(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID) {

		final File file =
			this.germplasmListTemplateExportService.export(cropName, programUUID);

		final HttpHeaders headers = new HttpHeaders();
		headers
			.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		headers.add(HttpHeaders.CONTENT_TYPE, String.format("%s;charset=utf-8", FileUtils.detectMimeType(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	@ApiIgnore
	@ApiOperation(value = "Customize the view of the germplasm list data.")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/view", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Void> saveGermplasmListDataView(@PathVariable final String cropName,
		@PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final List<GermplasmListDataUpdateViewDTO> view) {
		this.germplasmListDataService.saveGermplasmListDataView(listId, view);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
