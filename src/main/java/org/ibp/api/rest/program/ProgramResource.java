
package org.ibp.api.rest.program;

import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.java.program.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/program")
public class ProgramResource {

	@Autowired
	private ProgramService programService;

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<List<ProgramSummary>> listPrograms(@RequestParam final String cropName) {
		return new ResponseEntity<>(this.programService.listProgramsByCropName(cropName), HttpStatus.OK);
	}
}
