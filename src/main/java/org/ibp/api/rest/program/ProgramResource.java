
package org.ibp.api.rest.program;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.program.ProgramBasicDetailsDto;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

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

    @Deprecated // IBP-4301 (fixes dao to consider admin roles), IBP-4466 (add optional crop param to /programs)
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
    public ResponseEntity<List<ProgramDTO>> listPrograms(
        @RequestParam(required = false) final String cropName,
        @RequestParam(required = false) final String programNameContainsString,
        @ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable
    ) {

        final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
        programSearchRequest.setLoggedInUserId(this.securityService.getCurrentlyLoggedInUser().getUserid());
        programSearchRequest.setCommonCropName(cropName);
        programSearchRequest.setProgramNameContainsString(programNameContainsString);

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

    @ApiOperation(value = "Get program by programUUID", notes = "Get program by programUUID")
    @RequestMapping(value = "/crops/{cropName}/programs/{programUUID}", method = RequestMethod.GET)
    public ResponseEntity<ProgramDTO> getProgramByProgramUUID(@PathVariable final String cropName,
        @PathVariable final String programUUID) {
        final ProgramDTO programDTO = this.programService.getByUUIDAndCrop(cropName, programUUID);
        return new ResponseEntity<>(programDTO, HttpStatus.OK);
    }

    @ApiOperation(value = "Create program", notes = "Create program")
    @RequestMapping(value = "/crops/{cropName}/programs", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'ADD_PROGRAM')")
    public ResponseEntity<ProgramDTO> createProgram(@PathVariable final String cropName,
        @RequestBody final ProgramBasicDetailsDto programBasicDetailsDto) {
        final ProgramDTO programDTO = this.programService.createProgram(cropName, programBasicDetailsDto);
        return new ResponseEntity<>(programDTO, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete program", notes = "Delete program")
    @RequestMapping(value = "/crops/{cropName}/programs/{programUUID}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT')")
    public ResponseEntity<Void> deleteProgram(@PathVariable final String cropName,
        @PathVariable final String programUUID) {
        this.programService.deleteProgram(programUUID);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Edit a program", notes = "Edit a program")
    @RequestMapping(value = "/crops/{cropName}/programs/{programUUID}", method = RequestMethod.PATCH)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_PROGRAMS', 'MANAGE_PROGRAM_SETTINGS')")
    public ResponseEntity<Void> editProgram(@PathVariable final String cropName, @PathVariable final String programUUID,
        @RequestBody final ProgramBasicDetailsDto programBasicDetailsDto) {
        final boolean updateExecuted = this.programService.editProgram(cropName, programUUID, programBasicDetailsDto);
        return new ResponseEntity<>((updateExecuted) ? HttpStatus.OK : HttpStatus.NO_CONTENT);
    }

    private boolean hasAdmin() {
        return this.request.isUserInRole(PermissionsEnum.ADMIN.name())
            || this.request.isUserInRole(PermissionsEnum.CROP_MANAGEMENT.name())
            || this.request.isUserInRole(PermissionsEnum.ADMINISTRATION.name())
            || this.request.isUserInRole(PermissionsEnum.SITE_ADMIN.name());
    }
}
