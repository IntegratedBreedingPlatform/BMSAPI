
package org.ibp.api.rest.dataset;

import java.util.Arrays;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.java.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
	
	@ApiOperation(value = "Add Dataset Variable", notes = "Add Dataset Variable")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/variables", method = RequestMethod.PUT)
	@Transactional
	public ResponseEntity<MeasurementVariable> addTrait(@PathVariable final String crop, @PathVariable final Integer studyId,
			@PathVariable final Integer datasetId, @RequestBody final DatasetVariable datasetTrait) {
		
		final MeasurementVariable variable = this.studyDatasetService.addDatasetVariable(  studyId, datasetId, datasetTrait);
		return new ResponseEntity<>(variable, HttpStatus.OK);
	}
	
	@ApiOperation(value ="Remove dataset variables", notes = "Remove a set of variables from dataset")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/variables", method = RequestMethod.DELETE)
	@Transactional
	public ResponseEntity<Void> removeVariables(@PathVariable final String crop, @PathVariable final Integer studyId,
			@PathVariable final Integer datasetId,  @RequestParam(value = "variableIds", required = true) final Integer[] variableIds) {
		this.studyDatasetService.removeVariables(studyId, datasetId, Arrays.asList(variableIds));
		return new ResponseEntity<>(HttpStatus.OK);
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
