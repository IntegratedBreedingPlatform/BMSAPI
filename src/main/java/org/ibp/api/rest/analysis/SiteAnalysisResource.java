package org.ibp.api.rest.analysis;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.impl.analysis.MeansImportRequest;
import org.ibp.api.java.analysis.SiteAnalysisService;
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

@Api(value = "Site Analysis Service")
@Controller
public class SiteAnalysisResource {

	@Autowired
	private SiteAnalysisService siteAnalysisService;

	@ApiOperation(value = "Create means dataset", notes = "Create means dataset")
	@RequestMapping(value = "/crops/{cropName}/single-site-analysis/means", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<DatasetDTO> createMeansDataset(@PathVariable final String cropName,
		@RequestBody final MeansImportRequest meansImportRequest) {
		final DatasetDTO meansDataset = this.siteAnalysisService.createMeansDataset(meansImportRequest);
		meansDataset.setCropName(cropName);
		return new ResponseEntity<>(meansDataset, HttpStatus.OK);
	}

}
