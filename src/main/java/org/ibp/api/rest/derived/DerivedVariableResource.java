package org.ibp.api.rest.derived;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.java.derived.DerivedVariableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.Map;

@Api(value = "Derived Variable Services")
@Controller
@RequestMapping("/crops")
public class DerivedVariableResource {

	@Resource
	private DerivedVariableService derivedVariableService;

	@ApiOperation(value = "Execute Derived Variable", notes = "Execute the formula of a derived variable for each observation of specified instances.")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variable/calculate", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> calculate(
		@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestBody final CalculateVariableRequest request) {

		final Map<String, Object> result =
			this.derivedVariableService.execute(studyId, datasetId, request.getVariableId(), request.getGeoLocationIds());

		return new ResponseEntity<>(result, HttpStatus.OK);

	}

}
