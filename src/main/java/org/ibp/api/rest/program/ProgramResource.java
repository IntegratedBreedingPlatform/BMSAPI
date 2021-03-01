
package org.ibp.api.rest.program;

import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.program.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<List<ProgramSummary>> listPrograms(@PathVariable final String cropName) {
        if (request.isUserInRole(PermissionsEnum.ADMIN.name())
            || request.isUserInRole(PermissionsEnum.CROP_MANAGEMENT.name())
            || request.isUserInRole(PermissionsEnum.ADMINISTRATION.name())
            || request.isUserInRole(PermissionsEnum.SITE_ADMIN.name())) {
            return new ResponseEntity<>(this.programService.listProgramsByCropName(cropName), HttpStatus.OK);
        } else {
            final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
            programSearchRequest.setLoggedInUserId(this.securityService.getCurrentlyLoggedInUser().getUserid());
            programSearchRequest.setCommonCropName(cropName);
            return new ResponseEntity<>(this.programService.listProgramsByCropNameAndUser(programSearchRequest), HttpStatus.OK);
        }
    }
}
