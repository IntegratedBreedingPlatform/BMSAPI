package org.ibp.api.brapi.v2.program;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.ibp.api.Util;
import org.ibp.api.brapi.v1.common.*;
import org.ibp.api.brapi.v1.program.ProgramEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Api(value = "BrAPI V2 Program Services")
@Controller(value = "ProgramResourceBrapiV2")
public class ProgramResourceBrapi {

    @Autowired
    private ProgramService programService;

    @ApiOperation(value = "List Programs", notes = "Get a list of programs.")
    @RequestMapping(value = "/{commonCropName}/brapi/v2/programs", method = RequestMethod.GET)
    @ResponseBody
    @JsonView(BrapiView.BrapiV2.class)
    public ResponseEntity<EntityListResponse<ProgramDetailsDto>> listPrograms(
            @ApiParam(value = "Filter by the common crop name. Exact match.")
            @PathVariable final String commonCropName,
            @ApiParam(value = "Program filter to only return trials associated with given program id.", required = false)
            @RequestParam(value="programDbId", required = false) final String programDbId,
            @ApiParam(value = "Filter by program name. Exact match.", required = false) @RequestParam(value = "programName",
                    required = false) final String programName,
            @ApiParam(value = "Filter by program abbreviation. Exact match.", required = false) @RequestParam(value = "abbreviation",
                    required = false) final String abbreviation,
            @ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false) @RequestParam(value = "page",
                    required = false) final Integer currentPage,
            @ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false) @RequestParam(value = "pageSize",
                    required = false) final Integer pageSize) {


        final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
        final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

        final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
        programSearchRequest.setProgramDbId(programDbId);
        programSearchRequest.setProgramName(programName);
        programSearchRequest.setCommonCropName(commonCropName);


        PagedResult<ProgramDetailsDto> pagedResult = null;
        if (StringUtils.isBlank(abbreviation)) {
            pagedResult = new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<ProgramDetailsDto>() {
                @Override
                public long getCount() {
                    return ProgramResourceBrapi.this.programService.countProgramsByFilter(programSearchRequest);
                }

                @Override
                public List<ProgramDetailsDto> getResults(final PagedResult<ProgramDetailsDto> pagedResult) {
                    final int currPage = pagedResult.getPageNumber() + 1;
                    return ProgramResourceBrapi.this.programService.getProgramsByFilter(currPage, pagedResult.getPageSize(), programSearchRequest);
                }
            });
        }

        if (!Util.isNullOrEmpty(pagedResult) && pagedResult.getTotalResults() > 0) {
            return new ProgramEntityResponse().getEntityListResponseResponseEntity(pagedResult);
        }
        final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message",  "program not found."));
        final Metadata metadata = new Metadata(null, status);

        return new ResponseEntity<>(new EntityListResponse().withMetadata(metadata), HttpStatus.NOT_FOUND);
    }




}
