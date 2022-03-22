package org.ibp.api.rest.analysis;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.impl.analysis.MeansImportRequest;
import org.generationcp.middleware.service.impl.analysis.SummaryStatisticsImportRequest;
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
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/datasets/means", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<DatasetDTO> createMeansDataset(@PathVariable final String cropName,
		@PathVariable final String programUUID, @PathVariable final Integer studyId,
		@RequestBody final MeansImportRequest meansImportRequest) {
		final DatasetDTO meansDataset = this.siteAnalysisService.createMeansDataset(studyId, meansImportRequest);
		meansDataset.setCropName(cropName);
		return new ResponseEntity<>(meansDataset, HttpStatus.OK);
	}

	@ApiOperation(value = "Create summary statistics dataset", notes = "Create summary statistics dataset")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/datasets/summary-statistics", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<DatasetDTO> createSummaryStatisticsDataset(@PathVariable final String cropName,
		@PathVariable final String programUUID, @PathVariable final Integer studyId,
		@RequestBody final SummaryStatisticsImportRequest summaryStatisticsImportRequest) {
		final DatasetDTO summaryStatisticsDataset =
			this.siteAnalysisService.createSummaryStatisticsDataset(studyId, summaryStatisticsImportRequest);
		summaryStatisticsDataset.setCropName(cropName);
		return new ResponseEntity<>(summaryStatisticsDataset, HttpStatus.OK);
	}

}
