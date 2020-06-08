package org.ibp.api.rest.inventory.manager;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.InventoryView;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotSearchMetadata;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.domain.search.SearchDto;
import org.ibp.api.java.impl.middleware.inventory.common.InventoryLock;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.common.SearchRequestDtoResolver;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
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

	@Autowired
	private ExtendedLotListValidator extendedLotListValidator;

	@Autowired
	private SearchRequestDtoResolver searchRequestDtoResolver;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private InventoryLock inventoryLock;

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
					try {
						inventoryLock.lockRead();
						return LotResource.this.lotService.searchLots(searchDTO, pageable);
					} finally {
						inventoryLock.unlockRead();
					}
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
	public ResponseEntity<SingleEntityResponse<String>> createLot(
		@PathVariable final String cropName,
		@ApiParam("Lot to be created")
		@RequestBody final LotGeneratorInputDto lotGeneratorInputDto) {
		final SingleEntityResponse<String> singleEntityResponse =
			new SingleEntityResponse<String>(this.lotService.saveLot(lotGeneratorInputDto));
		return new ResponseEntity<>(singleEntityResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Update Lots", notes = "Update one or more Lots")
	@RequestMapping(value = "/crops/{cropName}/lot-lists", method = RequestMethod.PATCH)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('UPDATE_LOTS')")
	@ResponseBody
	public ResponseEntity<Void> updateLots(
		@PathVariable final String cropName,
		@ApiParam("Request with fields to update and criteria to update") @RequestBody final LotUpdateRequestDto lotRequest) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.inventoryCommonValidator.validateSearchCompositeDto(lotRequest.getSearchComposite(), errors);
		final LotsSearchDto searchDTO = this.searchRequestDtoResolver.getLotsSearchDto(lotRequest.getSearchComposite());

		final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDTO, null);
		if (lotRequest.getSearchComposite().getSearchRequest() == null) {
			this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(extendedLotDtos, lotRequest.getSearchComposite().getItemIds());
		}

		try {
			inventoryLock.lockWrite();
			this.lotService.updateLots(extendedLotDtos, lotRequest);
		} finally {
			inventoryLock.unlockWrite();
		}
		this.lotService.updateLots(extendedLotDtos, lotRequest);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);

	}

	@ApiOperation(value = "Create list of lots with an initial balance", notes = "Create list of lots with an initial balance")
	@RequestMapping(
		value = "/crops/{cropName}/lot-lists",
		method = RequestMethod.POST)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('IMPORT_LOTS')")
	public ResponseEntity<Void> importLotsWithInitialBalance(@PathVariable final String cropName,
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
		final List<LocationDto> locations = this.locationService.getLocations(LotResource.STORAGE_LOCATION_TYPE, null, false, null);

		final File file = this.lotTemplateExportServiceImpl.export(locations, units);
		final HttpHeaders headers = new HttpHeaders();
		headers
			.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve metadata for a lot search", notes = "It will retrieve metadata for a lot search")
	@RequestMapping(value = "/crops/{cropName}/lots/metadata", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('VIEW_LOTS')")
	public ResponseEntity<LotSearchMetadata> getLotSearchMetadata(
		@PathVariable final String cropName, //
		@ApiParam("List of lots to get metadata, use a searchId or a list of lot ids")
		@RequestBody final SearchCompositeDto<Integer, String> searchCompositeDto) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.inventoryCommonValidator.validateSearchCompositeDto(searchCompositeDto, errors);
		final LotsSearchDto searchDTO = this.searchRequestDtoResolver.getLotsSearchDto(searchCompositeDto);

		if (searchCompositeDto.getSearchRequest() == null) {
			final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDTO, null);
			this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(extendedLotDtos, searchCompositeDto.getItemIds());
		}
		try {
			inventoryLock.lockRead();
			return new ResponseEntity<>(this.lotService.getLotsSearchMetadata(searchDTO), HttpStatus.OK);
		} finally {
			inventoryLock.unlockRead();
		}
	}

	@ApiOperation(value = "Close Lots", notes = "Close a collection of lots")
	@RequestMapping(value = "/crops/{cropName}/lots/close", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('CLOSE_LOTS')")
	public ResponseEntity<Void> closeLots(
		@PathVariable final String cropName, //
		@ApiParam("List of lots to be closed, use a searchId or a list of lot ids")
		@RequestBody final SearchCompositeDto<Integer, String> searchCompositeDto) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), LotService.class.getName());
		this.inventoryCommonValidator.validateSearchCompositeDto(searchCompositeDto, errors);
		final LotsSearchDto searchDTO = this.searchRequestDtoResolver.getLotsSearchDto(searchCompositeDto);

		if (searchCompositeDto.getSearchRequest() == null) {
			final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDTO, null);
			this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(extendedLotDtos, searchCompositeDto.getItemIds());
		}
		try {
			inventoryLock.lockWrite();
			this.lotService.closeLots(searchDTO);
		} finally {
			inventoryLock.unlockWrite();
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve a lot", notes = "It will retrieve lot by lotUUID")
	@RequestMapping(value = "/crops/{cropName}/lots/{lotUUID}", method = RequestMethod.GET)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('VIEW_LOTS')")
	@ResponseBody
	@JsonView(InventoryView.LotView.class)
	public ResponseEntity<ExtendedLotDto> getLot(@PathVariable final String cropName, @PathVariable final String lotUUID) {

		final LotsSearchDto searchDTO = new LotsSearchDto();
		searchDTO.setLotUUIDs(Arrays.asList(lotUUID));

		try {
			inventoryLock.lockRead();
			final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDTO, null);

			if (!extendedLotDtos.isEmpty()) {
				return new ResponseEntity<>(extendedLotDtos.get(0), HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} finally {
			inventoryLock.unlockRead();
		}
	}

}
