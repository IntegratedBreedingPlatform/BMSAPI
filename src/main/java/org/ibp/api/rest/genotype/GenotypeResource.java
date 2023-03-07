package org.ibp.api.rest.genotype;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.genotype.GenotypeDTO;
import org.generationcp.middleware.domain.genotype.GenotypeImportRequestDto;
import org.generationcp.middleware.domain.genotype.GenotypeSearchRequestDTO;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.genotype.GenotypeService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(value = "Genotype Services")
@Controller
public class GenotypeResource {

    @Autowired
    private GenotypeService genotypeService;

    @ApiOperation(value = "Import genotypes", notes = "Import genotypes")
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')")
    @RequestMapping(value = "/crops/{cropName}/genotypes/{listId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<List<Integer>> importGenotypes(@PathVariable final String cropName,
                                                         @PathVariable final Integer listId,
                                                        @RequestParam(required = false) final String programUUID,
                                                        @RequestBody final List<GenotypeImportRequestDto> genotypeImportRequestDtos) {
        return new ResponseEntity<>(this.genotypeService.importGenotypes(programUUID, listId, genotypeImportRequestDtos), HttpStatus.OK);
    }

    @ApiOperation(value = "It will retrieve all genotypes of the study",
            notes = "It will retrieve all genotypes of the study")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'VIEW_STUDIES')")
    @RequestMapping(value = "/{cropname}/programs/{programUUID}/studies/{studyId}/genotypes/table", method = RequestMethod.POST)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "Results page you want to retrieve (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "Number of records per page."),
            @ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
                    value = "Sorting criteria in the format: property,asc|desc. ")
    })
    public ResponseEntity<List<GenotypeDTO>> getGenotypesTable(final @PathVariable String cropname,
                                                               @PathVariable final String programUUID,
                                                               @PathVariable final Integer studyId, @RequestBody final GenotypeSearchRequestDTO genotypeSearchRequestDTO,
                                                               final @ApiIgnore @PageableDefault(page = 0, size = PagedResult.DEFAULT_PAGE_SIZE) Pageable pageable) {

        genotypeSearchRequestDTO.setStudyId(studyId);
        return new PaginatedSearch()
                .getPagedResult(() -> this.genotypeService.countGenotypes(genotypeSearchRequestDTO),
                        () -> this.genotypeService.countFilteredGenotypes(genotypeSearchRequestDTO),
                        () -> this.genotypeService.searchGenotypes(genotypeSearchRequestDTO, pageable), pageable
                );
    }
}
