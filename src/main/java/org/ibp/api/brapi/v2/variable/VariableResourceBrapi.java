package org.ibp.api.brapi.v2.variable;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.brapi.VariableServiceBrapi;
import org.ibp.api.brapi.v1.common.*;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.search.SearchDto;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Api(value = "BrAPI V2 Variable Services")
@Controller(value = "VariableResourceBrapiV2")
public class VariableResourceBrapi {

    @Autowired
    private VariableServiceBrapi variableServiceBrapi;

    @Autowired
    private SearchRequestService searchRequestService;

    @ApiOperation(value = "Search Observation 'Variables'", notes = "Submit a search request for Observation 'Variables'")
    @RequestMapping(value = "/{crop}/brapi/v2/search/variables", method = RequestMethod.POST)
    @ResponseBody
    @JsonView(BrapiView.BrapiV2.class)
    public ResponseEntity<SingleEntityResponse<SearchDto>> postSearchObservationVariables(
            @PathVariable final String crop,
            @RequestBody final VariableSearchRequestDTO variableSearchRequestDTO) {
        final SearchDto searchDto =
                new SearchDto(this.searchRequestService.saveSearchRequest(variableSearchRequestDTO, VariableSearchRequestDTO.class)
                        .toString());
        final SingleEntityResponse<SearchDto> singleVariableSearchRequestDTO = new SingleEntityResponse<>(searchDto);

        return new ResponseEntity<>(singleVariableSearchRequestDTO, HttpStatus.OK);
    }

    @ApiOperation(value = "Get the results of a Observation variables search request", notes = "Get the results of a Observation 'variables' search request")
    @RequestMapping(value = "/{crop}/brapi/v2/search/variables/{searchResultsDbId}", method = RequestMethod.GET)
    @ResponseBody
    @JsonView(BrapiView.BrapiV2.class)
    public ResponseEntity<EntityListResponse<VariableDTO>> getObservationVariableSearchResults(
            @PathVariable final String crop,
            @PathVariable final String searchResultsDbId,
            @RequestParam(value = "page",
                    required = false) final Integer currentPage,
            @ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
            @RequestParam(value = "pageSize",
                    required = false) final Integer pageSize) {

        final VariableSearchRequestDTO requestDTO;
        try {
            requestDTO =
                    (VariableSearchRequestDTO) this.searchRequestService
                            .getSearchRequest(Integer.valueOf(searchResultsDbId), VariableSearchRequestDTO.class);
        } catch (final NumberFormatException | MiddlewareException e) {
            return new ResponseEntity<>(
                    new EntityListResponse<VariableDTO>(new Result<>(new ArrayList<>())).withMessage("no search request found"),
                    HttpStatus.NOT_FOUND);
        }

        return getSearchResults(crop, requestDTO, currentPage, pageSize);
    }

    @ApiOperation(value = "Call to retrieve a list of observationVariables available in the system.", notes = "Get the Observation Variables")
    @RequestMapping(value = "/{crop}/brapi/v2/variables", method = RequestMethod.GET)
    @JsonView(BrapiView.BrapiV2.class)
    @ResponseBody
    public ResponseEntity<EntityListResponse<VariableDTO>> getObservationVariables(@PathVariable final String crop,
        @ApiParam(value = "Variable's unique ID")
        @RequestParam(value = "observationVariableDbId", required = false) final String observationVariableDbId,
        @ApiParam(value = "Variable's trait class (phenological, physiological, morphological, etc.)")
        @RequestParam(value = "traitClass", required = false) final String traitClass,
        @ApiParam(value = "The unique ID of a studies to filter on")
        @RequestParam(value = "studyDbId", required = false) final String studyDbId,
        @ApiParam(value = "An external reference ID. Could be a simple string or a URI. (use with externalReferenceSource parameter)")
        @RequestParam(value = "externalReferenceID", required = false) final String externalReferenceID,
        @ApiParam(value = "An identifier for the source system or database of an external reference (use with externalReferenceID parameter)")
        @RequestParam(value = "externalReferenceSource", required = false) final String externalReferenceSource,
        @ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
        @RequestParam(value = "page", required = false) final Integer currentPage,
        @ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
        @RequestParam(value = "pageSize", required = false) final Integer pageSize) {
        final VariableSearchRequestDTO requestDTO = new VariableSearchRequestDTO();
        if(!StringUtils.isEmpty(observationVariableDbId)) {
            requestDTO.setObservationVariableDbIds(Collections.singletonList(observationVariableDbId));
        }
        if(!StringUtils.isEmpty(traitClass)) {
            requestDTO.setTraitClasses(Collections.singletonList(traitClass));
        }
        if(!StringUtils.isEmpty(studyDbId)) {
            requestDTO.setStudyDbId(Collections.singletonList(studyDbId));
        }
        if(!StringUtils.isEmpty(externalReferenceID)) {
            requestDTO.setExternalReferenceIDs(Collections.singletonList(externalReferenceID));
        }
        if(!StringUtils.isEmpty(externalReferenceSource)) {
            requestDTO.setExternalReferenceSources(Collections.singletonList(externalReferenceSource));
        }

        return getSearchResults(crop, requestDTO, currentPage, pageSize);
    }

    private ResponseEntity<EntityListResponse<VariableDTO>> getSearchResults(String crop, VariableSearchRequestDTO requestDTO, Integer currentPage, Integer pageSize) {
        final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
        final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

        final PageRequest pageRequest = new PageRequest(finalPageNumber, finalPageSize);

        final PagedResult<VariableDTO> resultPage =
                new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<VariableDTO>() {

                    @Override
                    public long getCount() {
                        return VariableResourceBrapi.this.variableServiceBrapi.countObservationVariables(requestDTO);
                    }

                    @Override
                    public List<VariableDTO> getResults(final PagedResult<VariableDTO> pagedResult) {
                        final int pageNumber = pagedResult.getPageNumber() + 1;
                        return VariableResourceBrapi.this.variableServiceBrapi
                                .getObservationVariables(requestDTO, pageRequest);
                    }
                });

        final Result<VariableDTO> result = new Result<VariableDTO>().withData(resultPage.getPageResults());
        final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
                .withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

        final Metadata metadata = new Metadata().withPagination(pagination);

        final EntityListResponse<VariableDTO> entityListResponse = new EntityListResponse<>(metadata, result);

        return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
    }

}
