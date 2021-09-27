package org.ibp.api.brapi.v2.variable;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.search.SearchDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Api(value = "BrAPI V2 Variable Services")
@Controller
public class VariableResourceBrapi {

    @Autowired
    private SearchRequestService searchRequestService;

    @ApiOperation(value = "Search Observation 'Variables'", notes = "Submit a search request for Observation 'Variables'")
    @RequestMapping(value = "/{crop}/brapi/v2/search/variables", method = RequestMethod.POST)
    @ResponseBody
    @JsonView(BrapiView.BrapiV2.class)
    public ResponseEntity<SingleEntityResponse<SearchDto>> postSearchVariables(
            @PathVariable final String crop,
            @RequestBody final VariableSearchRequestDTO variableSearchRequestDTO) {
        final SearchDto searchDto =
                new SearchDto(this.searchRequestService.saveSearchRequest(variableSearchRequestDTO, VariableSearchRequestDTO.class)
                        .toString());
        final SingleEntityResponse<SearchDto> singleVariableSearchRequestDTO = new SingleEntityResponse<>(searchDto);

        return new ResponseEntity<>(singleVariableSearchRequestDTO, HttpStatus.OK);
    }
}
