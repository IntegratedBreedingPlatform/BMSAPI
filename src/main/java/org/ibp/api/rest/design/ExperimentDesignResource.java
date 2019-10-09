package org.ibp.api.rest.design;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.exception.BVDesignException;
import org.ibp.api.java.design.ExperimentDesignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Experiment Design Service")
@Controller
@RequestMapping("/crops")
public class ExperimentDesignResource {

	@Resource
	private ExperimentDesignService experimentDesignService;

	@ApiOperation(value = "Generate design", notes = "Generate design")
	@RequestMapping(value = "/{crop}/studies/{studyId}/design/", method = RequestMethod.POST)
	public ResponseEntity<List<MeasurementVariable>> generateDesign(@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@RequestBody final ExperimentDesignInput experimentDesignInput) throws BVDesignException {

		this.experimentDesignService.generateAndSaveDesign(studyId, experimentDesignInput);

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
