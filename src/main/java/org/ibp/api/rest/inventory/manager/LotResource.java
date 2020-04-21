package org.ibp.api.rest.inventory.manager;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.InventoryView;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotSearchMetadata;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.SearchCompositeDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.domain.search.SearchDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.inventory.manager.LotService;
import org.ibp.api.java.inventory.manager.LotTemplateExportService;
import org.ibp.api.java.location.LocationService;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Api(value = "Lot Services")
@RestController
public class LotResource {

	private static final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));
	private static final String HAS_MANAGE_LOTS = "hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_INVENTORY', 'MANAGE_LOTS')";

	@Autowired
	private LotService lotService;

	@Autowired
	private LotTemplateExportService lotTemplateExportServiceImpl;

	@Autowired
	private VariableService variableService;

	@Autowired
	LocationService locationService;

	@Autowired
	private SearchRequestService searchRequestService;

	@ApiOperation(value = "Post lot search", notes = "Post lot search")
	@RequestMapping(value = "/crops/{cropName}/lots/search", method = RequestMethod.POST)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('VIEW_LOTS')")
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<SearchDto>> postSearchLots(
		@PathVariable final String cropName, @RequestBody final LotsSearchDto lotsSearchDto) {
		final String searchRequestId =
			this.searchRequestService.saveSearchRequest(lotsSearchDto, LotsSearchDto.class).toString();

		final SearchDto searchDto = new SearchDto(searchRequestId);
		final SingleEntityResponse<SearchDto> singleEntityResponse = new SingleEntityResponse<SearchDto>(searchDto);

		return new ResponseEntity<>(singleEntityResponse, HttpStatus.OK);

	}

	@ApiOperation(value = "It will retrieve lots that matches search conditions", notes = "It will retrieve lots that matches search conditions")
	@RequestMapping(value = "/crops/{cropName}/lots/search", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
					value = "Results page you want to retrieve (0..N)"),
			@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
					value = "Number of records per page."),
			@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
					value = "Sorting criteria in the format: property(,asc|desc). " +
							"Default sort order is ascending. " +
							"Multiple sort criteria are supported.")
	})
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('VIEW_LOTS')")
	@ResponseBody
	@JsonView(InventoryView.LotView.class)
	public ResponseEntity<List<ExtendedLotDto>> getLots(@PathVariable final String cropName, //
		@RequestParam final Integer searchRequestId, @ApiIgnore
	final Pageable pageable) {

		final LotsSearchDto searchDTO = (LotsSearchDto) this.searchRequestService
			.getSearchRequest(searchRequestId, LotsSearchDto.class);

		final PagedResult<ExtendedLotDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<ExtendedLotDto>() {

				@Override
				public long getCount() {
					return LotResource.this.lotService.countSearchLots(searchDTO);
				}

				@Override
				public List<ExtendedLotDto> getResults(final PagedResult<ExtendedLotDto> pagedResult) {
					return LotResource.this.lotService.searchLots(searchDTO, pageable);
				}
			});

		final List<ExtendedLotDto> extendedLotDtos = resultPage.getPageResults();

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));

		return new ResponseEntity<>(extendedLotDtos, headers, HttpStatus.OK);

	}

	@ApiOperation(value = "Create Lot", notes = "Create a new lot")
	@RequestMapping(value = "/crops/{cropName}/lots", method = RequestMethod.POST)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('CREATE_LOTS')")
	@ResponseBody
	public ResponseEntity<Integer> createLot(
		@PathVariable final String cropName,
		@ApiParam("Lot to be created")
		@RequestBody final LotGeneratorInputDto lotGeneratorInputDto) {
		return new ResponseEntity<>(lotService.saveLot(lotGeneratorInputDto), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update Lots", notes = "Update one or more Lots")
	@RequestMapping(value = "/crops/{cropName}/lot-lists", method = RequestMethod.PATCH)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('UPDATE_LOTS')")
	@ResponseBody
	public ResponseEntity<Void> updateLots(
		@PathVariable final String cropName,
		@ApiParam("Request with fields to update and criteria to update") @RequestBody final LotUpdateRequestDto lotRequest) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		Integer searchRequestId = null;
		Set<Integer> lotIds = null;
		final SearchCompositeDto searchCompositeDto = lotRequest.getSearchComposite();
		if (searchCompositeDto != null) {
			searchRequestId = searchCompositeDto.getSearchRequestId();
			lotIds = searchCompositeDto.getItemIds();
		}
		final LotsSearchDto searchDTO = validateSearchComposite(searchRequestId, lotIds, errors);

		final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDTO, null);
		if (extendedLotDtos == null || (searchRequestId == null && lotIds != null && extendedLotDtos.size() != lotIds.size())) {
			errors.reject("lots.does.not.exist", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		this.lotService.updateLots(extendedLotDtos, lotRequest);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);

	}

	@ApiOperation(value = "Create list of lots with an initial balance", notes = "Create list of lots with an initial balance")
	@RequestMapping(
		value = "/crops/{crop}/lot-lists",
		method = RequestMethod.POST)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('IMPORT_LOTS')")
	public ResponseEntity<Void> importLotsWithInitialBalance(@PathVariable final String crop,
		@RequestBody final LotImportRequestDto lotImportRequestDto) {
		this.lotService.importLotsWithInitialTransaction(lotImportRequestDto);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Download Template as excel file", notes = "Download Template as excel file")
	@RequestMapping(
		value = "/crops/{cropName}/lot-lists/templates/xls",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('IMPORT_LOTS')")
	public ResponseEntity<FileSystemResource> getTemplate(@PathVariable final String cropName) {

		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final List<VariableDetails> units = this.variableService.getVariablesByFilter(variableFilter);
		final List<LocationDto> locations = locationService.getLocations(LotResource.STORAGE_LOCATION_TYPE, null, false, null);

		final File file = this.lotTemplateExportServiceImpl.export(locations, units);
		final HttpHeaders headers = new HttpHeaders();
		headers
			.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve metadata for a lot search", notes = "It will retrieve metadata for a lot search")
	@RequestMapping(value = "/crops/{cropName}/lots/metadata", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('VIEW_LOTS')")
	public ResponseEntity<LotSearchMetadata> getLotSearchMetadata(@PathVariable final String cropName, //
		@RequestParam(required = false) final Integer searchRequestId, @RequestParam(required = false) final Set<Integer> lotIds) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final LotsSearchDto searchDTO = validateSearchComposite(searchRequestId, lotIds, errors);

		if (searchRequestId == null) {
			final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDTO, null);
			if (extendedLotDtos.size() != lotIds.size()) {
				errors.reject("lots.does.not.exist", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}

		return new ResponseEntity<>(lotService.getLotsSearchMetadata(searchDTO), HttpStatus.OK);
	}

	@ApiOperation(value = "Close Lots", notes = "Close any lot")
	@RequestMapping(value = "/crops/{cropName}/lots/close", method = RequestMethod.PATCH)
	@ResponseBody
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('UPDATE_LOTS')")
	public ResponseEntity<Void> cancelPendingTransaction(
		@PathVariable final String cropName, //
		@ApiParam("List of lots to be close, use a searchId or a list of transaction ids")
		@RequestBody final SearchCompositeDto searchCompositeDto) {

		this.lotService.closeLots(searchCompositeDto);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// TODO Use SearchComposito.isValid
	private LotsSearchDto validateSearchComposite(
		final Integer searchRequestId,
		final Set<Integer> lotIds,
		final BindingResult errors) {

		// Validate that searchId or list of lots are provided
		if (searchRequestId == null && (lotIds == null || lotIds.isEmpty()) ||
			searchRequestId != null && (lotIds != null)) {
			errors.reject("lot.selection.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final LotsSearchDto searchDTO;
		if (searchRequestId != null) {
			searchDTO = (LotsSearchDto) this.searchRequestService.getSearchRequest(searchRequestId, LotsSearchDto.class);
		} else {
			searchDTO = new LotsSearchDto();
			searchDTO.setLotIds(new ArrayList<>(lotIds));
		}
		return searchDTO;
	}

}
