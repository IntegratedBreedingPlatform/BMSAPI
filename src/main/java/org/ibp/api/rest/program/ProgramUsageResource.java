package org.ibp.api.rest.program;

import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.program.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProgramUsageResource {

	@Autowired
	private ProgramService programService;

	@Autowired
	public SecurityService securityService;

	@ApiOperation(value = "Return the last program launched by the user", notes = "Return the last program launched by the user")
	@RequestMapping(value = "my-program-usage/last", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ProgramDTO> getLastLauchedProgram(
		@RequestParam final Integer userId) {
		return new ResponseEntity<>(this.programService.getLastOpenedProject(userId), HttpStatus.OK);
	}

	@ApiOperation(value = "Save the program selected by the user", notes = "Save the program selected by the user")
	@RequestMapping(value = "/crops/{cropName}/my-program-usage", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> saveSelectedProgram(@PathVariable final String cropName, @RequestParam(required = false) final String programUUID) {
		this.programService.saveOrUpdateProjectUserInfo(this.securityService.getCurrentlyLoggedInUser().getUserid(), programUUID);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
