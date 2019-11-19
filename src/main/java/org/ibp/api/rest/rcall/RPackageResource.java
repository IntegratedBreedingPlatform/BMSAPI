package org.ibp.api.rest.rcall;

import io.swagger.annotations.ApiOperation;
import org.ibp.api.domain.rpackage.RCallDTO;
import org.ibp.api.java.rpackage.RPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/r-packages")
public class RPackageResource {

	@Autowired
	private RPackageService rPackageService;

	@ApiOperation(value = "Get R Calls by R Package", notes = "Get R Calls by R Package")
	@RequestMapping(value = "/{packageId}/r-calls", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<RCallDTO>> getRCallsByPackageId(@PathVariable final Integer packageId) {
		return new ResponseEntity<>(this.rPackageService.getRCallsByPackageId(packageId), HttpStatus.OK);
	}

}
