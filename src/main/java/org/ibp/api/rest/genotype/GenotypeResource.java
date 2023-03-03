package org.ibp.api.rest.genotype;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.genotype.GenotypeImportRequestDto;
import org.ibp.api.java.genotype.GenotypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
}
