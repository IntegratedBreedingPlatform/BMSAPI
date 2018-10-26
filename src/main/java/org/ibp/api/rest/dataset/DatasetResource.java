package org.ibp.api.rest.dataset;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.java.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;

@Api(value = "Dataset Services")
@Controller
@RequestMapping("/crops")
public class DatasetResource {

	@Autowired
	private DatasetService studyDatasetService;

	@ApiOperation(value = "Count Phenotypes", notes = "Returns count of phenotypes for variables")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/variables/observations", method = RequestMethod.HEAD)
	public ResponseEntity<String> countPhenotypes(@PathVariable final String crop, @PathVariable final Integer studyId,
			@PathVariable final Integer datasetId,  @RequestParam(value = "variableIds", required = true) final Integer[] variableIds) {

		final long count = this.studyDatasetService.countPhenotypes(studyId, datasetId, Arrays.asList(variableIds));
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

	@ApiOperation(value = "Generate and save a dataset", notes = "Returns the basic information for the generated dataset")
	@RequestMapping(value = "/{cropName}/studies/{studyId}/datasets/generation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Observation> generateDataset(@PathVariable
	final String cropName, @PathVariable final Integer studyId, @RequestBody final DatasetGeneratorInput datasetGeneratorInput) {
		studyDatasetService.generateSubObservationDataset(studyId, datasetGeneratorInput);
		return null;
	}
}
