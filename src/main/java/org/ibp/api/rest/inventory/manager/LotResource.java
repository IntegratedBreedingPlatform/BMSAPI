package org.ibp.api.rest.inventory.manager;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.inventory.common.LotGeneratorBatchRequestDto;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.InventoryView;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotMergeRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotMultiUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotSearchMetadata;
import org.generationcp.middleware.domain.inventory.manager.LotSplitRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.Util;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.domain.search.SearchDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.common.InventoryLock;
import org.ibp.api.java.impl.middleware.inventory.manager.common.SearchRequestDtoResolver;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotMergeValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotSplitValidator;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Api(value = "Lot Services")
@RestController
public class LotResource {

	private static final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));
	private static final String HAS_MANAGE_LOTS = "hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_INVENTORY', 'MANAGE_LOTS')";

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
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Autowired
	private InventoryLock inventoryLock;

	@Autowired
	private LotMergeValidator lotMergeValidator;

	@Autowired
	private LotSplitValidator lotSplitValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@ApiOperation(value = "Post lot search", notes = "Post lot search")
	@RequestMapping(value = "/crops/{cropName}/lots/search", method = RequestMethod.POST)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('VIEW_LOTS')" + PermissionsEnum.HAS_CREATE_LOTS_BATCH)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<SearchDto>> postSearchLots(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final LotsSearchDto lotsSearchDto) {
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
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('VIEW_LOTS')" + PermissionsEnum.HAS_CREATE_LOTS_BATCH)
	@ResponseBody
	@JsonView(InventoryView.LotView.class)
	public ResponseEntity<List<ExtendedLotDto>> getLots(@PathVariable final String cropName, //
		@RequestParam(required = false) final String programUUID,//
		@RequestParam final Integer searchRequestId, @ApiIgnore final Pageable pageable) {

		final LotsSearchDto searchDTO = (LotsSearchDto) this.searchRequestService
			.getSearchRequest(searchRequestId, LotsSearchDto.class);

		final PagedResult<ExtendedLotDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<ExtendedLotDto>() {

				@Override
				public long getCount() {
					return 0;
				}

				@Override
				public long getFilteredCount() {
					return LotResource.this.lotService.countSearchLots(searchDTO);
				}

				@Override
				public List<ExtendedLotDto> getResults(final PagedResult<ExtendedLotDto> pagedResult) {
					try {
						LotResource.this.inventoryLock.lockRead();
						return LotResource.this.lotService.searchLots(searchDTO, pageable);
					} finally {
						LotResource.this.inventoryLock.unlockRead();
					}
				}
			});

		final List<ExtendedLotDto> extendedLotDtos = resultPage.getPageResults();

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Filtered-Count", Long.toString(resultPage.getFilteredResults()));

		return new ResponseEntity<>(extendedLotDtos, headers, HttpStatus.OK);

	}

	@ApiOperation(value = "Create Lot", notes = "Create a new lot")
	@RequestMapping(value = "/crops/{cropName}/lots", method = RequestMethod.POST)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('CREATE_LOTS')")
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<String>> createLot(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@ApiParam("Lot to be created")
		@RequestBody final LotGeneratorInputDto lotGeneratorInputDto) {
		final SingleEntityResponse<String> singleEntityResponse =
			new SingleEntityResponse<>(this.lotService.saveLot(programUUID, lotGeneratorInputDto));
		return new ResponseEntity<>(singleEntityResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Create multiple lots")
	@RequestMapping(value = "/crops/{cropName}/lots/generation", method = RequestMethod.POST)
	@PreAuthorize(HAS_MANAGE_LOTS + PermissionsEnum.HAS_CREATE_LOTS_BATCH)
	@ResponseBody
	public ResponseEntity<List<String>> createLots(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@ApiParam("Lot template for batch generation. Some fields are ignored (gid, lotId, etc). "
			+ "SearchComposite is a list of gids or a search id (internal usage) ")
		@RequestBody final LotGeneratorBatchRequestDto lotGeneratorBatchRequestDto) {

		return new ResponseEntity<>(this.lotService.createLots(programUUID, lotGeneratorBatchRequestDto), HttpStatus.OK);
	}

	@ApiOperation(value = "Update Lots", notes = "Update one or more Lots")
	@RequestMapping(value = "/crops/{cropName}/lot-lists", method = RequestMethod.PATCH)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('UPDATE_LOTS')")
	@ResponseBody
	public ResponseEntity<Void> updateLots(
		@PathVariable final String cropName, @RequestParam(required = false) final String programUUID,
		@ApiParam("Request with fields to update and criteria to update") @RequestBody final LotUpdateRequestDto lotRequest) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		List<ExtendedLotDto> extendedLotDtos = null;

		if ((lotRequest.getSingleInput() == null && lotRequest.getMultiInput() == null) || //
			(lotRequest.getSingleInput() != null && lotRequest.getMultiInput() != null)) {
			errors.reject("lot.update.invalid.input", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (lotRequest.getSingleInput() != null) {
			this.searchCompositeDtoValidator.validateSearchCompositeDto(lotRequest.getSingleInput().getSearchComposite(), errors);
			final LotsSearchDto searchDTO =
				this.searchRequestDtoResolver.getLotsSearchDto(lotRequest.getSingleInput().getSearchComposite());

			extendedLotDtos = this.lotService.searchLots(searchDTO, null);
			if (lotRequest.getSingleInput().getSearchComposite().getSearchRequest() == null) {
				this.extendedLotListValidator
					.validateAllProvidedLotUUIDsExist(extendedLotDtos, lotRequest.getSingleInput().getSearchComposite().getItemIds());
			}
		} else {
			final List<String> lotUIDs =
				lotRequest.getMultiInput().getLotList().stream().map(LotMultiUpdateRequestDto.LotUpdateDto::getLotUID)
					.collect(Collectors.toList());
			final LotsSearchDto lotsSearchDto = new LotsSearchDto();
			lotsSearchDto.setLotUUIDs(lotUIDs);
			extendedLotDtos = this.lotService.searchLots(lotsSearchDto, null);
			this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(extendedLotDtos, Sets.newHashSet(lotUIDs));
			this.extendedLotListValidator.validateLotUUIDsDuplicated(extendedLotDtos, lotUIDs);
		}

		try {
			this.inventoryLock.lockWrite();
			this.lotService.updateLots(programUUID, extendedLotDtos, lotRequest);
		} finally {
			this.inventoryLock.unlockWrite();
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);

	}

	@ApiOperation(value = "Create list of lots with an initial balance", notes = "Create list of lots with an initial balance")
	@RequestMapping(
		value = "/crops/{cropName}/lot-lists",
		method = RequestMethod.POST)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('IMPORT_LOTS') " + PermissionsEnum.HAS_IMPORT_GERMPLASM)
	public ResponseEntity<Void> importLotsWithInitialBalance(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID, @RequestBody final LotImportRequestDto lotImportRequestDto) {
		this.lotService.importLotsWithInitialTransaction(programUUID, lotImportRequestDto);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Download Template as excel file", notes = "Download Template as excel file")
	@RequestMapping(
		value = "/crops/{cropName}/lot-lists/templates/xls",
		method = RequestMethod.GET)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('IMPORT_LOTS')")
	public ResponseEntity<FileSystemResource> getTemplate(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID) {

		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final List<VariableDetails> units = this.variableService.getVariablesByFilter(variableFilter);
		final List<LocationDto> locations =
			this.locationService
				.getLocations(cropName, new LocationSearchRequest(programUUID, LotResource.STORAGE_LOCATION_TYPE, null, null, null, false),
					null);

		final File file = this.lotTemplateExportServiceImpl.export(locations, units);
		final HttpHeaders headers = new HttpHeaders();
		headers
			.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", FileUtils.sanitizeFileName(file.getName())));
		headers.add(HttpHeaders.CONTENT_TYPE, String.format("%s;charset=utf-8", FileUtils.detectMimeType(file.getName())));
		final FileSystemResource fileSystemResource = new FileSystemResource(file);
		return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve metadata for a lot search", notes = "It will retrieve metadata for a lot search")
	@RequestMapping(value = "/crops/{cropName}/lots/metadata", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('VIEW_LOTS')")
	public ResponseEntity<LotSearchMetadata> getLotSearchMetadata(
		@PathVariable final String cropName, //
		@RequestParam(required = false) final String programUUID, //
		@ApiParam("List of lots to get metadata, use a searchId or a list of lot ids")
		@RequestBody final SearchCompositeDto<Integer, String> searchCompositeDto) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.searchCompositeDtoValidator.validateSearchCompositeDto(searchCompositeDto, errors);
		final LotsSearchDto searchDTO = this.searchRequestDtoResolver.getLotsSearchDto(searchCompositeDto);

		if (searchCompositeDto.getSearchRequest() == null) {
			final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDTO, null);
			this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(extendedLotDtos, searchCompositeDto.getItemIds());
		}
		try {
			this.inventoryLock.lockRead();
			return new ResponseEntity<>(this.lotService.getLotsSearchMetadata(searchDTO), HttpStatus.OK);
		} finally {
			this.inventoryLock.unlockRead();
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
		this.searchCompositeDtoValidator.validateSearchCompositeDto(searchCompositeDto, errors);
		final LotsSearchDto searchDTO = this.searchRequestDtoResolver.getLotsSearchDto(searchCompositeDto);

		if (searchCompositeDto.getSearchRequest() == null) {
			final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDTO, null);
			this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(extendedLotDtos, searchCompositeDto.getItemIds());
		}
		try {
			this.inventoryLock.lockWrite();
			this.lotService.closeLots(searchDTO);
		} finally {
			this.inventoryLock.unlockWrite();
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve a lot", notes = "It will retrieve lot by lotUUID")
	@RequestMapping(value = "/crops/{cropName}/lots/{lotUUID}", method = RequestMethod.GET)
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('VIEW_LOTS')")
	@ResponseBody
	@JsonView(InventoryView.LotView.class)
	public ResponseEntity<ExtendedLotDto> getLot(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@PathVariable final String lotUUID) {

		final LotsSearchDto searchDTO = new LotsSearchDto();
		searchDTO.setLotUUIDs(Arrays.asList(lotUUID));

		try {
			this.inventoryLock.lockRead();
			final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDTO, null);

			if (!extendedLotDtos.isEmpty()) {
				return new ResponseEntity<>(extendedLotDtos.get(0), HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} finally {
			this.inventoryLock.unlockRead();
		}
	}

	@ApiOperation(value = "Merge lots", notes = "Merge lots from the same germplasm. It keeps one lot and all the other lot balances will be then transferred to this lot and subsequently discarded.")
	@RequestMapping(value = "/crops/{cropName}/lots/merge", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize(HAS_MANAGE_LOTS
		+ " or hasAnyAuthority('MERGE_LOTS')")
	public ResponseEntity<Void> mergeLots(
		@PathVariable final String cropName, //
		@ApiParam("Lot template for merge action."
			+ "SearchComposite is a list of UUIDs or a search id (internal usage) ")
		@RequestBody final LotMergeRequestDto lotMergeRequestDto) {

		try {
			this.inventoryLock.lockWrite();

			this.lotMergeValidator.validateRequest(lotMergeRequestDto);

			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), LotService.class.getName());
			final SearchCompositeDto<Integer, String> searchComposite = lotMergeRequestDto.getSearchComposite();
			this.searchCompositeDtoValidator.validateSearchCompositeDto(searchComposite, errors);
			final LotsSearchDto searchDTO = this.searchRequestDtoResolver.getLotsSearchDto(searchComposite);
			if (searchComposite.getSearchRequest() == null) {
				final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDTO, null);
				this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(extendedLotDtos, searchComposite.getItemIds());
			}

			this.lotService.mergeLots(lotMergeRequestDto.getLotUUIDToKeep(), searchDTO);
		} finally {
			this.inventoryLock.unlockWrite();
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Split lot", notes = "It generates a new lot using an existing lot as the source for the initial deposit.")
	@RequestMapping(value = "/crops/{cropName}/lots/split", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize(HAS_MANAGE_LOTS
		+ " or hasAnyAuthority('SPLIT_LOT')")
	public ResponseEntity<Void> splitLot(
		@PathVariable final String cropName, //
		@RequestParam(required = false) final String programUUID,//
		@ApiParam("Lot template for merge action."
			+ "SearchComposite is a list of UUIDs or a search id (internal usage) ")
		@RequestBody final LotSplitRequestDto lotSplitRequestDto) {

		this.lotSplitValidator.validateRequest(lotSplitRequestDto);

		try {
			this.inventoryLock.lockWrite();
			this.lotService.splitLot(programUUID, lotSplitRequestDto);
		} finally {
			this.inventoryLock.unlockWrite();
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Returns lots for the given germplasm id", notes = "Returns lots for the given germplasm id.")
	@RequestMapping(value = "/crops/{cropName}/lots/germplasm/{gid}", method = RequestMethod.GET)
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

	@ResponseBody
	@JsonView(InventoryView.LotView.class)
	public ResponseEntity<List<ExtendedLotDto>> getLotsByGermplasmId(
		@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final LotStatus status,
		@ApiIgnore final Pageable pageable) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), LotService.class.getName());
		if (!Util.isPositiveInteger(String.valueOf(gid))) {
			errors.reject("gids.invalid", new String[] {gid.toString()}, "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		this.germplasmValidator.validateGermplasmId(errors, gid);
		if (errors.hasErrors()) {
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final LotsSearchDto searchDTO = new LotsSearchDto();
		searchDTO.setGids(Arrays.asList(gid));

		if (!Objects.isNull(status)) {
			searchDTO.setStatus(status.getIntValue());
		}

		final PagedResult<ExtendedLotDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<ExtendedLotDto>() {

				@Override
				public long getCount() {
					return LotResource.this.lotService.countSearchLots(searchDTO);
				}

				@Override
				public List<ExtendedLotDto> getResults(final PagedResult<ExtendedLotDto> pagedResult) {
					try {
						LotResource.this.inventoryLock.lockRead();
						return LotResource.this.lotService.searchLots(searchDTO, pageable);
					} finally {
						LotResource.this.inventoryLock.unlockRead();
					}
				}
			});

		final List<ExtendedLotDto> extendedLotDtos = resultPage.getPageResults();

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));

		return new ResponseEntity<>(extendedLotDtos, headers, HttpStatus.OK);

	}

}
