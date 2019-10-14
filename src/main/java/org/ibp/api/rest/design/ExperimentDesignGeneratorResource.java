package org.ibp.api.rest.design;

import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.ibp.api.java.design.DesignLicenseService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@Controller
public class ExperimentDesignGeneratorResource {

	@Resource
	private DesignLicenseService designLicenseService;

	@ApiOperation(value = "Gets all experiment design types supported for design generation")
	@RequestMapping(value= "/design/types", method = RequestMethod.GET)
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


	@ApiOperation(value = "Gets insertion manners for checks")
	@RequestMapping(value= "/design/checks/insertionManners", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<InsertionMannerItem>> retrieveCheckInsertionManners() {
		return new ResponseEntity<>(Arrays.asList(InsertionMannerItem.values()), HttpStatus.OK);
	}

	@ApiOperation(value = "Count number of days before license for design generator expires")
	@RequestMapping(value= "/design/generator/license/expiryDays", method = RequestMethod.HEAD)
	@ResponseBody
	public ResponseEntity<String> getLicenseExpiryDays() {
		final Integer expiryDays = this.designLicenseService.getExpiryDays();
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(expiryDays));
		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

}
