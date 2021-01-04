package org.ibp.api.rest.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.ibp.api.java.entrytype.EntryTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Entry Type Services")
@Controller
@RequestMapping("/crops")
public class EntryTypeResource {

	@Resource
	private EntryTypeService entryTypeService;

	@ApiOperation(value = "Get Entry Types",
		notes = "Get Entry Types")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/entry-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Enumeration>> getEntryTypes(final @PathVariable String cropname,
		@PathVariable final String programUUID) {
		final List<Enumeration> entryTypes =
			this.entryTypeService.getEntryTypes(programUUID);
		return new ResponseEntity<>(entryTypes, HttpStatus.OK);
	}

}
