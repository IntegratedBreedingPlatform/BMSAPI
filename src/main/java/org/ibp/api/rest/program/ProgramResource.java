
package org.ibp.api.rest.program;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.ibp.api.domain.common.PagedResult;
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
    public ResponseEntity<List<ProgramDTO>> listProgramsByCrop(@PathVariable final String cropName) {
		//TODO Review if this IF condition is required
        if (this.hasAdmin()) {
            return new ResponseEntity<>(this.programService.listProgramsByCropName(cropName), HttpStatus.OK);
        } else {
            final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
            programSearchRequest.setLoggedInUserId(this.securityService.getCurrentlyLoggedInUser().getUserid());
            programSearchRequest.setCommonCropName(cropName);
            return new ResponseEntity<>(this.programService.listProgramsByCropNameAndUser(programSearchRequest), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/programs", method = RequestMethod.GET)
	@ApiOperation(value = "List Programs",
		notes = "Returns the list of programs that the logged in user have access to")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "Number of records per page.")
    })
    public ResponseEntity<List<ProgramDTO>> listPrograms(final Pageable pageable) {

        final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
        programSearchRequest.setLoggedInUserId(this.securityService.getCurrentlyLoggedInUser().getUserid());

        final PagedResult<ProgramDTO> pagedResult = new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(),
            new SearchSpec<ProgramDTO>() {

                @Override
                public long getCount() {
                        return ProgramResource.this.programService
                            .countProgramsByFilter(programSearchRequest);
                }

                @Override
                public List<ProgramDTO> getResults(final PagedResult<ProgramDTO> pagedResult) {
                        return ProgramResource.this.programService
                            .getFilteredPrograms(pageable, programSearchRequest);
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
