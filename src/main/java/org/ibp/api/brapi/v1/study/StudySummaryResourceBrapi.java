package org.ibp.api.brapi.v1.study;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a> Study Summary services.
 */
@Api(value = "BrAPI Study Summary Services")
@Controller
public class StudySummaryResourceBrapi {

    @Autowired
    private StudyDataManager studyDataManager;

    @ApiOperation(value = "List of study summaries", notes = "Get a list of study summaries.")
    @RequestMapping(value = "/{crop}/brapi/v1/studies", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<StudySummariesDto> listStudySummaries(@PathVariable final String crop,
                                                           @ApiParam(value = "Program filter to only return studies associated with given program id.",
                                                                   required = false) @RequestParam(value = "programDbId", required = false) String programDbId,
                                                           @ApiParam(value = "Location filter to only return studies associated with given location id.",
                                                                   required = false) @RequestParam(value = "locationDbId", required = false) String locationDbId,
                                                           @ApiParam(value = "Season or year filter to only return studies associated with given season or year.",
                                                                   required = false) @RequestParam(value = "seasonDbId", required = false) String seasonDbId,
                                                           @ApiParam(value = "Page number to retrieve in case of multi paged results. Defaults to 1 (first page) if not supplied.",
                                                                   required = false) @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                                           @ApiParam(value = "Number of results to retrieve per page. Defaults to 100 if not supplied. Max page size allowed is 200.",
                                                                   required = false)
                                                           @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return null;
    }

}
