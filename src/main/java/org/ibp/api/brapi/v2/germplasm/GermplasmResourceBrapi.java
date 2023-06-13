package org.ibp.api.brapi.v2.germplasm;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmSearchRequest;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.GermplasmServiceBrapi;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.brapi.v1.germplasm.Germplasm;
import org.ibp.api.brapi.v2.BrapiResponseMessageGenerator;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.search.BrapiSearchDto;
import org.ibp.api.domain.search.SearchDto;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

import java.util.ArrayList;
import java.util.List;

@Api(value = "BrAPI v2 Gerplasm Services")
@Controller(value = "GermplasmResourceBrapiV2")
public class GermplasmResourceBrapi {

	@Autowired
	private GermplasmServiceBrapi germplasmService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private BrapiResponseMessageGenerator<GermplasmDTO> responseMessageGenerator;

	@ApiOperation(value = "Get a filtered list of Germplasm", notes = "Get a filtered list of Germplasm")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM','SEARCH_GERMPLASM')")
	@RequestMapping(value = "/{crop}/brapi/v2/germplasm", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<Germplasm>> getGermplasm(
		@PathVariable final String crop,
		@ApiParam(value = "Permanent unique identifier (DOI, URI, etc.). This filter is not supported for now.")
		@RequestParam(value = "germplasmPUI", required = false) final String germplasmPUI,
		@ApiParam(value = "Internal database identifier")
		@RequestParam(value = "germplasmDbId", required = false) final String germplasmDbId,
		@ApiParam(value = "Name of the germplasm")
		@RequestParam(value = "germplasmName", required = false) final String germplasmName,
		@ApiParam(value = "The common crop name")
		@RequestParam(value = "commonCropName", required = false) final String commonCropName,
		@ApiParam(value = "Unique identifiers for accessions within a genebank")
		@RequestParam(value = "accessionNumber", required = false) final String accessionNumber,
		@ApiParam(value = "collection filter is not supported. This value is ignored.")
		@RequestParam(value = "collection", required = false) final String collection,
		@ApiParam(value = "Genus name to identify germplasm")
		@RequestParam(value = "genus", required = false) final String genus,
		@ApiParam(value = "Search for Germplasm that are associated with a particular Study")
		@RequestParam(value = "studyDbId", required = false) final String studyDbId,
		@ApiParam(value = "Alternative name used to reference this germplasm")
		@RequestParam(value = "synonym", required = false) final String synonym,
		@ApiParam(value = "Search for Germplasm with this parent")
		@RequestParam(value = "parentDbId", required = false) final String parentDbId,
		@ApiParam(value = "Search for Germplasm with this child")
		@RequestParam(value = "progenyDbId", required = false) final String progenyDbId,
		@ApiParam(value = "Search for externalReferenceId")
		@RequestParam(value = "externalReferenceID", required = false) final String externalReferenceID,
		@ApiParam(value = "Search for externalReferenceSource")
		@RequestParam(value = "externalReferenceSource", required = false) final String externalReferenceSource,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION)
		@RequestParam(value = "page", required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION)
		@RequestParam(value = "pageSize", required = false) final Integer pageSize) {

		final GermplasmSearchRequest germplasmSearchRequest =
			this.getGermplasmSearchRequestDto(germplasmDbId, commonCropName, germplasmPUI, germplasmName, accessionNumber, studyDbId,
				synonym, genus, parentDbId,
				progenyDbId, externalReferenceID, externalReferenceSource);

		final PagedResult<GermplasmDTO> resultPage = this.getGermplasmDTOPagedResult(germplasmSearchRequest, currentPage, pageSize);
		final List<Germplasm> germplasmList = GermplasmMapper.mapGermplasm(resultPage.getPageResults());

		final Result<Germplasm> results = new Result<Germplasm>().withData(germplasmList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<Germplasm> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);

	}

	@ApiOperation(value = "Create new Germplasm entities on this server", notes = "Create new Germplasm entities on this server")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM')")
	@RequestMapping(value = "/{crop}/brapi/v2/germplasm", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<Germplasm>> saveGermplasm(@PathVariable final String crop,
		@RequestBody final List<GermplasmImportRequest> germplasmImportRequestList) {
		BaseValidator.checkNotNull(germplasmImportRequestList, "germplasm.import.list.null");

		final GermplasmImportResponse germplasmImportResponse = this.germplasmService.createGermplasm(crop, germplasmImportRequestList);
		final List<Germplasm> germplasmList = GermplasmMapper.mapGermplasm(germplasmImportResponse.getEntityList());
		final Result<Germplasm> results = new Result<Germplasm>().withData(germplasmList);

		final Metadata metadata = new Metadata().withStatus(this.responseMessageGenerator.getMessagesList(germplasmImportResponse));
		final EntityListResponse<Germplasm> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Update the details of an existing germplasm", notes = "Update the details of an existing germplasm")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM')")
	@RequestMapping(value = "/{crop}/brapi/v2/germplasm/{germplasmDbId}", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<Germplasm>> updateGermplasm(@PathVariable final String crop,
		@PathVariable final String germplasmDbId,
		@RequestBody final GermplasmUpdateRequest germplasmUpdateRequest) {
		BaseValidator.checkNotNull(germplasmUpdateRequest, "germplasm.import.list.null");

		final GermplasmDTO germplasmDTO = this.germplasmService.updateGermplasm(germplasmDbId, germplasmUpdateRequest);
		final ModelMapper modelMapper = GermplasmMapper.getInstance();
		final Germplasm germplasm = modelMapper.map(germplasmDTO, Germplasm.class);

		final SingleEntityResponse<Germplasm> singleGermplasmResponse = new SingleEntityResponse<>(germplasm);
		return new ResponseEntity<>(singleGermplasmResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Search germplasm", notes = "Submit a search request for germplasm")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM','SEARCH_GERMPLASM')")
	@RequestMapping(value = "/{crop}/brapi/v2/search/germplasm", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<BrapiSearchDto>> postSearchGermplasm(
		@PathVariable final String crop,
		@RequestBody final GermplasmSearchRequest germplasmSearchRequest) {
		final BrapiSearchDto searchDto =
			new BrapiSearchDto(this.searchRequestService.saveSearchRequest(germplasmSearchRequest, GermplasmSearchRequest.class)
				.toString());
		final SingleEntityResponse<BrapiSearchDto> singleGermplasmSearchResponse = new SingleEntityResponse<>(searchDto);

		return new ResponseEntity<>(singleGermplasmSearchResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Get search germplasm results", notes = "Get the results of germplasm search request")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM','SEARCH_GERMPLASM')")
	@RequestMapping(value = "/{crop}/brapi/v2/search/germplasm/{searchResultsDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<Germplasm>> getGermplasmSearchResults(
		@PathVariable final String crop,
		@PathVariable final String searchResultsDbId,
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize) {

		final GermplasmSearchRequest germplasmSearchRequest;
		try {
			germplasmSearchRequest =
				(GermplasmSearchRequest) this.searchRequestService
					.getSearchRequest(Integer.valueOf(searchResultsDbId), GermplasmSearchRequest.class);
		} catch (final NumberFormatException | MiddlewareException e) {
			return new ResponseEntity<>(
				new EntityListResponse<Germplasm>(new Result<>(new ArrayList<>())).withMessage("no search request found"),
				HttpStatus.NOT_FOUND);
		}

		final PagedResult<GermplasmDTO> resultPage =
			this.getGermplasmDTOPagedResult(germplasmSearchRequest, currentPage,
				pageSize);

		final List<Germplasm> germplasmList = GermplasmMapper.mapGermplasm(resultPage.getPageResults());

		final Result<Germplasm> results = new Result<Germplasm>().withData(germplasmList);
		final Pagination pagination = new Pagination().withPageNumber(currentPage).withPageSize(pageSize)
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<Germplasm> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);

	}

	private GermplasmSearchRequest getGermplasmSearchRequestDto(final String germplasmDbId, final String commonCropName,
		final String germplasmPUI, final String germplasmName,
		final String accessionNumber, final String studyDbId, final String synonym, final String genus, final String parentDbId,
		final String progenyDbId, final String externalReferenceId, final String externalReferenceSource) {
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		if (!StringUtils.isEmpty(germplasmDbId)) {
			germplasmSearchRequest.setGermplasmDbIds(Lists.newArrayList(germplasmDbId));
		}
		if (!StringUtils.isEmpty(commonCropName)) {
			germplasmSearchRequest.setCommonCropNames(Lists.newArrayList(commonCropName));
		}
		if (StringUtils.isNotEmpty(germplasmName)) {
			germplasmSearchRequest.setPreferredName(germplasmName);
		}
		if (StringUtils.isNotEmpty(studyDbId)) {
			germplasmSearchRequest.setStudyDbIds(Lists.newArrayList(studyDbId));
		}
		if (StringUtils.isNotEmpty(externalReferenceId)) {
			germplasmSearchRequest.setExternalReferenceIDs(Lists.newArrayList(externalReferenceId));
		}
		if (StringUtils.isNotEmpty(externalReferenceSource)) {
			germplasmSearchRequest.setExternalReferenceSources(Lists.newArrayList(externalReferenceSource));
		}
		if (StringUtils.isNotEmpty(synonym)) {
			germplasmSearchRequest.setSynonyms(Lists.newArrayList(synonym));
		}
		if (StringUtils.isNotEmpty(parentDbId)) {
			germplasmSearchRequest.setParentDbIds(Lists.newArrayList(parentDbId));
		}
		if (StringUtils.isNotEmpty(progenyDbId)) {
			germplasmSearchRequest.setProgenyDbIds(Lists.newArrayList(progenyDbId));
		}
		if (StringUtils.isNotEmpty(genus)) {
			germplasmSearchRequest.setGenus(Lists.newArrayList(genus));
		}
		if (StringUtils.isNotEmpty(accessionNumber)) {
			germplasmSearchRequest.setAccessionNumbers(Lists.newArrayList(accessionNumber));
		}
		if (StringUtils.isNotEmpty(germplasmPUI)) {
			germplasmSearchRequest.setGermplasmPUIs(Lists.newArrayList(germplasmPUI));
		}
		return germplasmSearchRequest;
	}

	private PagedResult<GermplasmDTO> getGermplasmDTOPagedResult(final GermplasmSearchRequest germplasmSearchRequest,
		final Integer currentPage, final Integer pageSize) {
		final Integer finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final Integer finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;
		return new PaginatedSearch()
			.executeBrapiSearch(finalPageNumber, finalPageSize,
				new SearchSpec<GermplasmDTO>() {

					@Override
					public long getCount() {
						return GermplasmResourceBrapi.this.germplasmService.countGermplasmDTOs(germplasmSearchRequest);
					}

					@Override
					public List<GermplasmDTO> getResults(final PagedResult<GermplasmDTO> pagedResult) {
						return GermplasmResourceBrapi.this.germplasmService
							.searchGermplasmDTO(germplasmSearchRequest, new PageRequest(finalPageNumber, finalPageSize));
					}
				});
	}

}
