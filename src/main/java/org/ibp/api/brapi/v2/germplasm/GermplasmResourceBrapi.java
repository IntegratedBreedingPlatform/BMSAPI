package org.ibp.api.brapi.v2.germplasm;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import liquibase.util.StringUtils;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.germplasm.Germplasm;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "BrAPI v2 Gerplasm Services")
@Controller(value = "GermplasmResourceBrapiV2")
public class GermplasmResourceBrapi {

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	ResourceBundleMessageSource messageSource;

	@ApiOperation(value = "Get a filtered list of Germplasm", notes = "Get a filtered list of Germplasm")
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
		@ApiParam(value = "externalReferenceId filter is not supported for now. This value is ignored.")
		@RequestParam(value = "externalReferenceId", required = false) final String externalReferenceId,
		@ApiParam(value = "externalReferenceId filter is not supported for now. This value is ignored.")
		@RequestParam(value = "externalReferenceSource", required = false) final String externalReferenceSource,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION)
		@RequestParam(value = "page", required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION)
		@RequestParam(value = "pageSize", required = false) final Integer pageSize) {

		final GermplasmSearchRequestDto germplasmSearchRequestDTO =
			this.getGermplasmSearchRequestDto(germplasmPUI, germplasmName, accessionNumber, studyDbId, synonym, genus, parentDbId, progenyDbId);
		if (!StringUtils.isEmpty(germplasmDbId)) {
			germplasmSearchRequestDTO.setGermplasmDbIds(Lists.newArrayList(germplasmDbId));
		}
		if (!StringUtils.isEmpty(commonCropName)){
			germplasmSearchRequestDTO.setCommonCropNames(Lists.newArrayList(commonCropName));
		}

		final PagedResult<GermplasmDTO> resultPage = this.getGermplasmDTOPagedResult(germplasmSearchRequestDTO, currentPage, pageSize);
		final List<Germplasm> germplasmList = this.mapGermplasm(resultPage.getPageResults());

		final Result<Germplasm> results = new Result<Germplasm>().withData(germplasmList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<Germplasm> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);

	}

	private List<Germplasm> mapGermplasm(final List<GermplasmDTO> germplasmDTOList) {
		final List<Germplasm> germplasmList = new ArrayList<>();
		final ModelMapper mapper = new ModelMapper();
		if (!CollectionUtils.isEmpty(germplasmDTOList)) {
			for (final GermplasmDTO germplasmDTO : germplasmDTOList) {
				final Germplasm germplasm = mapper.map(germplasmDTO, Germplasm.class);
				germplasmList.add(germplasm);
			}
		}
		return germplasmList;
	}

	@ApiOperation(value = "Create new Germplasm entities on this server", notes = "Create new Germplasm entities on this server")
	@RequestMapping(value = "/{crop}/brapi/v2/germplasm", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<Germplasm>> getGermplasm(@PathVariable final String crop,
		@RequestBody final List<GermplasmImportRequest> germplasmImportRequestList) {
		BaseValidator.checkNotNull(germplasmImportRequestList, "germplasm.import.list.null");


		final GermplasmImportResponse germplasmImportResponse = this.germplasmService.createGermplasm(crop, germplasmImportRequestList);
		final List<Germplasm> germplasmList = this.mapGermplasm(germplasmImportResponse.getGermplasmList());
		final Result<Germplasm> results = new Result<Germplasm>().withData(germplasmList);

		final Map<String, String> messages = new HashMap<>();
		messages.put("INFO", germplasmImportResponse.getStatus());
		if (!CollectionUtils.isEmpty(germplasmImportResponse.getErrors())) {
			int index = 1;
			for (final ObjectError error : germplasmImportResponse.getErrors()) {
				messages.put("ERROR" + index++, this.getMessage(error.getCode(), error.getArguments()));
			}
		}
		final Metadata metadata = new Metadata().withStatus(Lists.newArrayList(messages));
		final EntityListResponse<Germplasm> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	private GermplasmSearchRequestDto getGermplasmSearchRequestDto(final String germplasmPUI, final String germplasmName,
		final String accessionNumber, final String studyDbId, final String synonym, final String genus, final String parentDbId, final String progenyDbId) {
		final GermplasmSearchRequestDto germplasmSearchRequestDTO = new GermplasmSearchRequestDto();
		germplasmSearchRequestDTO.setPreferredName(germplasmName);
		germplasmSearchRequestDTO.setStudyDbId(studyDbId);
		if (synonym != null) {
			germplasmSearchRequestDTO.setGermplasmNames(Lists.newArrayList(synonym));
		}
		germplasmSearchRequestDTO.setParentDbId(parentDbId);
		germplasmSearchRequestDTO.setProgenyDbId(progenyDbId);
		if (genus != null){
			germplasmSearchRequestDTO.setGermplasmGenus(Lists.newArrayList(genus));
		}
		if (accessionNumber != null){
			germplasmSearchRequestDTO.setAccessionNumbers(Lists.newArrayList(accessionNumber));
		}

		if (germplasmPUI != null) {
			germplasmSearchRequestDTO.setGermplasmPUIs(Lists.newArrayList(germplasmPUI));
		}
		return germplasmSearchRequestDTO;
	}

	private PagedResult<GermplasmDTO> getGermplasmDTOPagedResult(final GermplasmSearchRequestDto germplasmSearchRequestDTO,
		final Integer currentPage, final Integer pageSize) {
		final Integer finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final Integer finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;
		return new PaginatedSearch()
			.executeBrapiSearch(finalPageNumber, finalPageSize,
				new SearchSpec<GermplasmDTO>() {

					@Override
					public long getCount() {
						return GermplasmResourceBrapi.this.germplasmService.countGermplasmDTOs(germplasmSearchRequestDTO);
					}

					@Override
					public List<GermplasmDTO> getResults(final PagedResult<GermplasmDTO> pagedResult) {
						return GermplasmResourceBrapi.this.germplasmService
							.searchGermplasmDTO(germplasmSearchRequestDTO, new PageRequest(finalPageNumber, finalPageSize));
					}
				});
	}

	private String getMessage(final String code, final Object[] arguments) {
		return this.messageSource.getMessage(code, arguments, LocaleContextHolder.getLocale());
	}

}
