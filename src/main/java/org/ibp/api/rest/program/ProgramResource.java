package org.ibp.api.rest.program;

import java.util.List;

import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.java.program.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/program")
public class ProgramResource {

	@Autowired
	private ProgramService programService;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ResponseEntity<List<ProgramSummary>> listPrograms()  {
		return new ResponseEntity<>(programService.listAllPrograms(), HttpStatus.OK);
	}
}
