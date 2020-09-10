package org.ibp.api.rest.design;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.java.design.ExperimentalDesignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Experimental Design Type Service")
@Controller
@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')" + PermissionsEnum.HAS_MANAGE_STUDIES_VIEW)
@RequestMapping("/crops")
public class ExperimentalDesignTypeResource {

	@Resource
	private ExperimentalDesignService experimentalDesignService;

	@ApiOperation(value = "Gets all experimental design types supported for design generation")
	@RequestMapping(value= "/{crop}/experimental-design-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ExperimentDesignType>> retrieveDesignTypes(@PathVariable final String crop, @RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.experimentalDesignService.getExperimentalDesignTypes(),
			HttpStatus.OK);
	}


}
