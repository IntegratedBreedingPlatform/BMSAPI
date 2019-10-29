package org.ibp.api.rest.design;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.java.design.ExperimentalDesignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Experimental Design Type Service")
@Controller
@RequestMapping("/crops")
public class ExperimentalDesignTypeResource {

	@Resource
	private ExperimentalDesignService experimentalDesignService;

	@ApiOperation(value = "Gets all experimental design types supported for design generation")
	@RequestMapping(value= "/{crop}/experimental-design-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ExperimentDesignType>> retrieveDesignTypes(@PathVariable final String crop) {
		return new ResponseEntity<>(this.experimentalDesignService.getExperimentalDesignTypes(),
			HttpStatus.OK);
	}


}
