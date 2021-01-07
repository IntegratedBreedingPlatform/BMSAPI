
package org.ibp.api.rest.program;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class ProgramResource {

    @Autowired
    private ProgramService programService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SecurityService securityService;

    @RequestMapping(value = "/crops/{cropName}/programs", method = RequestMethod.GET)
    public ResponseEntity<List<ProgramSummary>> listProgramsByCrop(@PathVariable final String cropName) {
        if (this.hasAdmin()) {
            return new ResponseEntity<>(this.programService.listProgramsByCropName(cropName), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(this.programService.listProgramsByCropNameAndUser(this.securityService.getCurrentlyLoggedInUser(), cropName), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/programs", method = RequestMethod.GET)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "Number of records per page.")
    })
    public ResponseEntity<List<ProgramDTO>> listPrograms(final Pageable pageable) {

        final PagedResult<ProgramDTO> pagedResult = new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(),
            new SearchSpec<ProgramDTO>() {

                @Override
                public long getCount() {
                    if (ProgramResource.this.hasAdmin()) {
                        return ProgramResource.this.programService.countPrograms();
                    } else {
                        return ProgramResource.this.programService
                            .countProgramsByUser(ProgramResource.this.securityService.getCurrentlyLoggedInUser());
                    }
                }

                @Override
                public List<ProgramDTO> getResults(final PagedResult<ProgramDTO> pagedResult) {
                    if (ProgramResource.this.hasAdmin()) {
                        return ProgramResource.this.programService.listPrograms(pageable);
                    } else {
                        return ProgramResource.this.programService
                            .listProgramsByUser(pageable, ProgramResource.this.securityService.getCurrentlyLoggedInUser());
                    }
                }
            });

        final List<ProgramDTO> programs = pagedResult.getPageResults();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Long.toString(pagedResult.getTotalResults()));

        return new ResponseEntity<>(programs, headers, HttpStatus.OK);
    }

    private boolean hasAdmin() {
        return request.isUserInRole(PermissionsEnum.ADMIN.name())
            || request.isUserInRole(PermissionsEnum.CROP_MANAGEMENT.name())
            || request.isUserInRole(PermissionsEnum.ADMINISTRATION.name())
            || request.isUserInRole(PermissionsEnum.SITE_ADMIN.name());
    }
}
