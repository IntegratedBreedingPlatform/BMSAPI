
package org.ibp.api.rest.dataset;

import java.util.Arrays;

import org.ibp.api.java.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Dataset Services")
@Controller
@RequestMapping("/crops")
public class DatasetResource {

	@Autowired
	private DatasetService studyDatasetService;

	@ApiOperation(value = "Count Phenotypes", notes = "Returns count of phenotypes for variables")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/variables/observations", method = RequestMethod.HEAD)
	@Transactional
	public ResponseEntity<String> countPhenotypes(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestParam(value = "variableIds", required = true) final Integer[] variableIds) {

		final long count = this.studyDatasetService.countPhenotypes(studyId, datasetId, Arrays.asList(variableIds));
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

	@ApiOperation(value = "Count Phenotypes for specific instance (environment)", notes = "Returns count of phenotypes for specific instance (environment)")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/{instanceId}", method = RequestMethod.HEAD)
	@Transactional
	public ResponseEntity<String> countPhenotypesByInstance(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @PathVariable final Integer instanceId) {

		final long count = this.studyDatasetService.countPhenotypesByInstance(studyId, datasetId, instanceId);
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

}
