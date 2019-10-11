package org.ibp.api.rest.design.type;

import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Controller
public class ExperimentDesignTypeResource {

	@ApiOperation(value = "Gets all experiment design types supported for design generation")
	@RequestMapping(value= "/design-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ExperimentDesignType>> retrieveDesignTypes() {
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
