package org.ibp.api.rest.cop;

import io.swagger.annotations.ApiOperation;
import org.ibp.api.java.cop.CalculateCOPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/crops")
public class COPResource {

	@Autowired
	private CalculateCOPService calculateCOPService;

	@ApiOperation(value = "", notes = "")
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}/cop/export", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<COPExportStudyResponse> exportStudy(@PathVariable final String cropName, @PathVariable final String programUUID,
		@RequestBody final COPExportStudy COPExportStudy) {
		final COPExportStudyResponse COPExportStudyResponse = calculateCOPService.exportStudy(COPExportStudy);
		return new ResponseEntity<>(COPExportStudyResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "", notes = "")
	@RequestMapping(value = "/{cropName}/programs/{programUUID}/studies/{studyId}/cop/export/permissions", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> permissions(@PathVariable final String cropName, @PathVariable final String programUUID,
		@RequestBody final COPPermissions COPPermissions) throws IOException {

		final COPPermissionsResponce COPPermissionsResponce = calculateCOPService.permissions(COPPermissions);
		return new ResponseEntity(COPPermissionsResponce, HttpStatus.OK);
	}

}