package org.ibp.api.rest.design;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Api(value = "Experimental Design Type Service")
@Controller
@RequestMapping("/crops")
public class ExperimentalDesignTypeResource {

	@ApiOperation(value = "Gets all experimental design types supported for design generation")
	@RequestMapping(value= "/{crop}/experimental-design-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ExperimentDesignType>> retrieveDesignTypes(@PathVariable final String crop) {
		final List<ExperimentDesignType> designTypes = new ArrayList<>();

		designTypes.add(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK);
		designTypes.add(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK);
		designTypes.add(ExperimentDesignType.ROW_COL);
		designTypes.add(ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK);
		designTypes.add(ExperimentDesignType.CUSTOM_IMPORT);
		designTypes.add(ExperimentDesignType.ENTRY_LIST_ORDER);
		designTypes.add(ExperimentDesignType.P_REP);

		return new ResponseEntity<>(designTypes, HttpStatus.OK);
	}


}
