package org.ibp.api.brapi.v2;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.search_request.brapi.v2.SampleSearchRequestDTO;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.sample.SampleObservationDto;
import org.ibp.api.brapi.v1.common.*;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Api(value = "BrAPI v2 Sample Services")
@Controller(value = "SampleResourceBrapiV2")
public class SampleResourceBrapi {

    @Autowired
    private SampleService sampleService;


    @ApiOperation(value = "Get samples", notes = "Get samples")
    @RequestMapping(value = "/{crop}/brapi/v2/samples", method = RequestMethod.GET)
    @JsonView(BrapiView.BrapiV2.class)
    @ResponseBody
    public ResponseEntity<EntityListResponse<SampleObservationDto>> getSamples(@PathVariable final String crop,
        @ApiParam(value = "the internal DB id for a sample")
        @RequestParam(value = "sampleDbId", required = false) final String sampleDbId,
        @ApiParam(value = "the internal DB id for an observation unit where a sample was taken from")
        @RequestParam(value = "observationUnitDbId", required = false) final String observationUnitDbId,
        @ApiParam(value = "the internal DB id for a plate of samples")
        @RequestParam(value = "plateDbId", required = false) final String plateDbId,
        @ApiParam(value = "the internal DB id for a germplasm")
        @RequestParam(value = "germplasmDbId", required = false) final String germplasmDbId,
        @ApiParam(value = "Filter by study DbId")
        @RequestParam(value = "studyDbId", required = false) final String studyDbId,
        @ApiParam(value = "An external reference ID. Could be a simple string or a URI. (use with externalReferenceSource parameter)")
        @RequestParam(value = "externalReferenceID", required = false) final String externalReferenceID,
        @ApiParam(value = "An identifier for the source system or database of an external reference (use with externalReferenceID parameter)")
        @RequestParam(value = "externalReferenceSource", required = false) final String externalReferenceSource,
        @ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
        @RequestParam(value = "page", required = false) final Integer currentPage,
        @ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
        @RequestParam(value = "pageSize", required = false) final Integer pageSize) {

        final SampleSearchRequestDTO requestDTO = new SampleSearchRequestDTO(sampleDbId, observationUnitDbId, plateDbId, germplasmDbId, studyDbId, externalReferenceID, externalReferenceSource);

        final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
        final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

        final PageRequest pageRequest = new PageRequest(finalPageNumber, finalPageSize);

        final Result<SampleObservationDto> result = new Result<SampleObservationDto>();
        final Pagination pagination = new Pagination();

        final Metadata metadata = new Metadata().withPagination(pagination);
        final EntityListResponse<SampleObservationDto> entityListResponse = new EntityListResponse<>(metadata, result);

        return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
    }
}
