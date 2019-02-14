package org.ibp.api.rest.derived;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.domain.common.ErrorResponse;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.derived.DerivedVariableService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

@Api(value = "Derived Variable Services")
@Controller
@RequestMapping("/crops")
public class DerivedVariableResource {

	@Resource
	private DerivedVariableService derivedVariableService;

	@Resource
	private ResourceBundleMessageSource resourceBundleMessageSource;

	@ApiOperation(value = "Execute Derived Variable", notes = "Execute the formula of a derived variable for each observation of specified instances.")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variable/calculate", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> calculate(
		@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestBody final CalculateVariableRequest request) {

		try {
			final Map<String, Object> result =
				this.derivedVariableService.execute(studyId, datasetId, request.getVariableId(), request.getGeoLocationIds());
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (final ApiRequestValidationException e) {
			final ErrorResponse errorResponse = new ErrorResponse();
			final ObjectError objectError = e.getErrors().get(0);
			errorResponse.addError(
				resourceBundleMessageSource.getMessage(objectError.getCode(), objectError.getArguments(), LocaleContextHolder.getLocale()));
			return new ResponseEntity(errorResponse, HttpStatus.CONFLICT);
		}

	}

	@ResponseBody
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variable/dependencies", method = RequestMethod.GET)
	public ResponseEntity<Set<String>> dependencyVariables(
		@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {
		return new ResponseEntity<>(this.derivedVariableService.getDependencyVariables(studyId, datasetId), HttpStatus.OK);
	}

}
