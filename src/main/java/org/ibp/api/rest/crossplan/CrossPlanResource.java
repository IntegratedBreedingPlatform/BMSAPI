package org.ibp.api.rest.crossplan;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.crossplan.CrossPlanSearchRequest;
import org.generationcp.middleware.api.crossplan.CrossPlanSearchResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.crossplan.CrossPlanService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(value = "CrossPlan Services")
@Controller
@RequestMapping("/crops")
public class CrossPlanResource {

    @Autowired
    CrossPlanService crossPlanService;


    @ApiOperation("Search crossPlan")
    @RequestMapping(value = "/{cropName}/programs/{programUUID}/cross-plan/search", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "Number of records per page."),
            @ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
                    value = "Sorting criteria in the format: property,asc|desc. ")
    })
    @ResponseBody
    public ResponseEntity<List<CrossPlanSearchResponse>> searchCrossPlan(@PathVariable final String cropName,
                                                                         @PathVariable final String programUUID,
                                                                         @RequestBody final CrossPlanSearchRequest crossPlanSearchRequest,
                                                                         @ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable
    ) {
        return new PaginatedSearch().getPagedResult(
                () -> this.crossPlanService.countSearchCrossPlans(programUUID, crossPlanSearchRequest),
                () -> this.crossPlanService.searchCrossPlans(programUUID, crossPlanSearchRequest, pageable),
                pageable);
    }
}
