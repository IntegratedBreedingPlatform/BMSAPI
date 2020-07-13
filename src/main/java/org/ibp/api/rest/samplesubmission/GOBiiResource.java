package org.ibp.api.rest.samplesubmission;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.api.gobii.GOBiiContactDTO;
import org.generationcp.middleware.service.api.gobii.GOBiiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.List;

@Api(value = "GOBii Services")
@Controller
public class GOBiiResource {

	@Autowired
	private GOBiiService goBiiService;

	@Autowired
	private Environment environment;

	private Boolean gobiiStatus;

	@PostConstruct
	public void resolveGobiiURL() {
		gobiiStatus = Boolean.valueOf(environment.getProperty("gobii.submission.enabled"));
	}

	@ApiOperation(value = "Get GOBii Submission status", notes = "Get all GOBii contacts")
	@RequestMapping(value = "/gobii-submission-status", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Boolean> getGobiiStubmissionStatus() {
		return new ResponseEntity<>(gobiiStatus, HttpStatus.OK);
	}

	@ApiOperation(value = "Get all GOBii contacts", notes = "Get all GOBii contacts")
	@RequestMapping(value = "/gobii-contacts", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GOBiiContactDTO>> get() {
		return new ResponseEntity<>(this.goBiiService.getAllGOBiiContacts(), HttpStatus.OK);
	}

}
