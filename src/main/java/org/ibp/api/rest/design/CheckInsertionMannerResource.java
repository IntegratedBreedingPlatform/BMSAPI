package org.ibp.api.rest.design;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.domain.ontology.TermSummary;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Api(value = "Check Insertion Manner Service")
@Controller
@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES')" + PermissionsEnum.HAS_MANAGE_STUDIES_VIEW)
@RequestMapping("/crops")
public class CheckInsertionMannerResource {

	@ApiOperation(value = "Gets insertion manners for checks")
	@RequestMapping(value= "/{crop}/check-insertion-manners", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<TermSummary>> retrieveCheckInsertionManners(@PathVariable final String crop, @RequestParam(required = false) final String programUUID) {
		final List<TermSummary> terms = new ArrayList<>();
		final ModelMapper map = new ModelMapper();
		for (final InsertionMannerItem item : InsertionMannerItem.values()) {
			terms.add(map.map(item, TermSummary.class));
		}
		return new ResponseEntity<>(terms, HttpStatus.OK);
	}

}
