
package org.ibp.api.rest.dataset;

import java.util.Arrays;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.dataset.Observation;
import org.ibp.api.domain.dataset.ObservationValue;
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
	public ResponseEntity<MeasurementVariable> addTrait(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestBody final DatasetVariable datasetTrait) {

		final MeasurementVariable variable = this.studyDatasetService.addDatasetVariable(studyId, datasetId, datasetTrait);
		return new ResponseEntity<>(variable, HttpStatus.OK);
	}

	@ApiOperation(value = "Update Observation", notes = "Update Observation")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/{observationUnitId}/observations/{observationId}", method = RequestMethod.PATCH)
	@Transactional
	public ResponseEntity<Observation> updateObservation(
		@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @PathVariable final Integer observationUnitId, @PathVariable final Integer observationId,
		@RequestBody final ObservationValue observationValue) {

		final Observation observation = this.studyDatasetService.updatePhenotype(observationUnitId, observationId, observationValue);
		return new ResponseEntity<>(observation, HttpStatus.OK);
	}

}
