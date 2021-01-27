package org.ibp.api.rest.tool;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.workbench.ToolDTO;
import org.ibp.api.java.tool.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "Tool Services")
@Controller
public class ToolResource {

	@Autowired
	private ToolService toolService;

	@ApiOperation(value = "Returns tools navigation links")
	@RequestMapping(value = "/tools", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ToolDTO>> getTools(
		@RequestParam final String cropName,
		@RequestParam final Integer programId) {
		return new ResponseEntity<>(this.toolService.getTools(cropName, programId), HttpStatus.OK);
	}

}
