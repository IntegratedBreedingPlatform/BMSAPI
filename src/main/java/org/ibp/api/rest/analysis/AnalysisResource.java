package org.ibp.api.rest.analysis;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.analysis.MeansRequestDto;
import org.ibp.api.java.analysis.AnalysisService;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(value = "Analysis Service")
@Controller
public class AnalysisResource {

	@Autowired
	private AnalysisService analysisService;

	@ApiOperation(value = "Create means dataset", notes = "Create means dataset")
	@RequestMapping(value = "/crops/{cropName}/single-site-analysis/means", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity generateDataset(@PathVariable final String cropName,
		@RequestBody final MeansRequestDto meansRequestDto) {
		this.analysisService.importMeans(meansRequestDto);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
