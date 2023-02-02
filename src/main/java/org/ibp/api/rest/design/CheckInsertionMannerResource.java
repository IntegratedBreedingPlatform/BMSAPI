package org.ibp.api.rest.design;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.ibp.api.domain.ontology.TermSummary;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Api(value = "Check Insertion Manner Service")
@Controller
@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'VIEW_STUDIES')")
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
