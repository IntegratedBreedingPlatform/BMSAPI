package org.ibp.api.brapi.v2.program;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.ibp.api.brapi.v1.common.*;
import org.ibp.api.brapi.v1.program.Program;
import org.ibp.api.brapi.v1.program.ProgramEntityResponseBuilder;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "BrAPI V2 Program Services")
@Controller(value = "ProgramResourceBrapiV2")
public class ProgramResourceBrapi {

    @Autowired
    private ProgramService programService;

    @Autowired
    private SecurityService securityService;

    @ApiOperation(value = "Get filtered list of breeding Programs", notes = "Get a filtered list of breeding Programs. This list can be filtered by common crop name to narrow results to a specific crop.")
    @RequestMapping(value = "/brapi/v2/programs", method = RequestMethod.GET)
    @ResponseBody
    @JsonView(BrapiView.BrapiV2.class)
    public ResponseEntity<EntityListResponse<Program>> listPrograms(
            @ApiParam(value = "Filter by the common crop name. Exact match.", required = false)
            @RequestParam(value = "commonCropName", required = false) final String commonCropName,
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


        try {
            final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
            final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

            final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
            programSearchRequest.setProgramDbId(programDbId);
            programSearchRequest.setProgramName(programName);
            programSearchRequest.setAbbreviation(abbreviation);
            programSearchRequest.setLoggedInUserId(this.securityService.getCurrentlyLoggedInUser().getUserid());
            if (!StringUtils.isBlank(commonCropName)) {
                programSearchRequest.setCommonCropName(commonCropName);
            }
            final PagedResult<ProgramDetailsDto> pagedResult = new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<ProgramDetailsDto>() {
                @Override
                public long getCount() {
                    return ProgramResourceBrapi.this.programService.countProgramsByFilter(programSearchRequest);
                }

                @Override
                public List<ProgramDetailsDto> getResults(final PagedResult<ProgramDetailsDto> pagedResult) {
                    final int currPage = pagedResult.getPageNumber() + 1;
                    return ProgramResourceBrapi.this.programService.getProgramsByFilter(new PageRequest(currPage, pagedResult.getPageSize()), programSearchRequest);
                }
            });
            return ProgramEntityResponseBuilder.getEntityListResponseResponseEntity(pagedResult);
        } catch (final ApiRuntimeException | AccessDeniedException apiRuntimeException) {
            return ProgramEntityResponseBuilder.getEntityListResponseResponseEntityNotFound(apiRuntimeException.getMessage());
        }

    }




}
