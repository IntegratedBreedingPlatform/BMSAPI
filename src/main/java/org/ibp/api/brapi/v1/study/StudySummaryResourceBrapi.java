package org.ibp.api.brapi.v1.study;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
                                                                        required = false) @RequestParam(value = "programDbId", required = false) final String programDbId,
                                                                @ApiParam(value = "Location filter to only return studies associated with given location id.",
                                                                        required = false) @RequestParam(value = "locationDbId", required = false) final String locationDbId,
                                                                @ApiParam(value = "Season or year filter to only return studies associated with given season or year.",
                                                                        required = false) @RequestParam(value = "seasonDbId", required = false) final String seasonDbId,
                                                                @ApiParam(value = "Page number to retrieve in case of multi paged results. Defaults to 1 (first page) if not supplied.",
                                                                        required = false) @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                                                @ApiParam(value = "Number of results to retrieve per page. Defaults to 100 if not supplied. Max page size allowed is 200.",
                                                                        required = false)
                                                                @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        PagedResult<StudySummary> resultPage =
                new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<StudySummary>() {

                    @Override
                    public long getCount() {
                        return StudySummaryResourceBrapi.this.studyDataManager.countAllPrograms(programDbId, locationDbId, seasonDbId);
                    }

                    @Override
                    public List<StudySummary> getResults(
                            PagedResult<StudySummary> pagedResult) {
                        return StudySummaryResourceBrapi.this.studyDataManager.findPagedProjects(programDbId, locationDbId, seasonDbId,
                                pagedResult.getPageSize(), pagedResult.getPageNumber());
                    }
                });

        List<StudySummaryDto> studies = new ArrayList<>();
        PropertyMap<StudySummary, StudySummaryDto> studySummaryMapper = new PropertyMap<StudySummary, StudySummaryDto>() {
            protected void configure() {
                map().setLocationDbId(source.getLocationId());
                map().setSeasons(source.getSeasons());
                map().setYears(source.getYears());
                map().setName(source.getName());
                map().setOptionalInfo(source.getOptionalInfo());
                map().setProgramDbId(source.getProgramDbId());
                map().setStudyDbId(source.getStudyDbid());
                map().setStudyType(source.getType());
            }
        };

        ModelMapper modelMapper = new ModelMapper();

        modelMapper.addMappings(studySummaryMapper);

        for (StudySummary mwStudy : resultPage.getPageResults()) {
            StudySummaryDto studySummaryDto = modelMapper.map(mwStudy, StudySummaryDto.class);
            studies.add(studySummaryDto);
        }

        Result<org.ibp.api.brapi.v1.study.StudySummaryDto> results = new Result<StudySummaryDto>().withData(studies);
        Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
                .withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

        Metadata metadata = new Metadata().withPagination(pagination);
        StudySummariesDto studiesList = new StudySummariesDto().setMetadata(metadata).setResult(results);

        return new ResponseEntity<StudySummariesDto>(studiesList, HttpStatus.OK);
    }
}

