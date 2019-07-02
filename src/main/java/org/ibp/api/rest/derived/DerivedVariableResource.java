package org.ibp.api.rest.derived;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.ibp.api.exception.OverwriteDataException;
import org.ibp.api.java.derived.DerivedVariableService;
import org.ibp.api.java.impl.middleware.derived.DerivedVariableServiceImpl;
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

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Api(value = "Derived Variable Services")
@Controller
@RequestMapping("/crops")
public class DerivedVariableResource {

	@Resource
	private DerivedVariableService derivedVariableService;

	@ApiOperation(value = "Execute Derived Variable", notes = "Execute the formula of a derived variable for each observation of specified instances.")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variables/calculation", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> calculate(
		@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestBody final CalculateVariableRequest request) {

		try {
			final Map<String, Object> result =
				this.derivedVariableService
					.execute(studyId, datasetId, request.getVariableId(), request.getGeoLocationIds(), request.getInputVariableDatasetMap(),
						request.isOverwriteExistingData());
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (final OverwriteDataException e2) {
			final Map<String, Object> result = new HashMap<>();
			result.put(DerivedVariableServiceImpl.HAS_DATA_OVERWRITE_RESULT_KEY, true);
			return new ResponseEntity<>(result, HttpStatus.OK);
		}

	}

	@ApiOperation(value = "Get Missing Formula Variables", notes =
		"Gets the list of formula variables that are not yet added in study.")
	@ResponseBody
	@RequestMapping(value = "/{crop}/studies/{studyId}/derived-variables/{variableId}/formula-variables/missing", method = RequestMethod.GET)
	public ResponseEntity<Set<FormulaVariable>> missingFormulaVariables(
		@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer variableId) {
		return new ResponseEntity<>(this.derivedVariableService.getMissingFormulaVariablesInStudy(studyId, variableId), HttpStatus.OK);
	}

	@ApiOperation(value = "Get All Formula Variables", notes =
		"Gets the list of formula variables in study.")
	@ResponseBody
	@RequestMapping(value = "/{crop}/studies/{studyId}/derived-variables/formula-variables", method = RequestMethod.GET)
	public ResponseEntity<Set<FormulaVariable>> formulaVariables(
		@PathVariable final String crop,
		@PathVariable final Integer studyId) {
		return new ResponseEntity<>(this.derivedVariableService.getFormulaVariablesInStudy(studyId),
			HttpStatus.OK);
	}

	@ApiOperation(value = "Count Calculated Traits", notes = "Count the calculated traits (derived traits) in a specified dataset(s)")
	@ResponseBody
	@RequestMapping(value = "/{crop}/studies/{studyId}/derived-variables", method = RequestMethod.HEAD)
	public ResponseEntity<String> countCalculatedVariables(
		@PathVariable final String crop,
		@PathVariable final Integer studyId, @RequestParam(value = "datasetIds") final Set<Integer> datasetIds) {

		final long count = this.derivedVariableService.countCalculatedVariablesInDatasets(studyId, datasetIds);
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

	@ApiOperation(value = "Get a map of formula variables and dataset(s) from where they belong to", notes = "")
	@ResponseBody
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variables/{variableId}/formula-variables/dataset-map", method = RequestMethod.GET)
	public ResponseEntity<Map<Integer, Map<String, Object>>> getFormulaVariableDatasetMap(
		@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@PathVariable final Integer variableId) {
		return new ResponseEntity<>(this.derivedVariableService.getFormulaVariableDatasetMap(studyId, datasetId, variableId), HttpStatus.OK);
	}

}
