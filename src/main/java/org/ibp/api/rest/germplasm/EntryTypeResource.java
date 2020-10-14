package org.ibp.api.rest.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.ibp.api.java.entrytype.EntryTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

	@ApiOperation(value = "Get Study Entry Types",
		notes = "Get Study Entry Types")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/entry-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<Enumeration>> getStudyEntryTypes(final @PathVariable String cropname,
		@PathVariable final String programUUID) {
		final List<Enumeration> entryTypes =
			this.entryTypeService.getEntryTypes(programUUID);
		return new ResponseEntity<>(entryTypes, HttpStatus.OK);
	}

	@ApiOperation(value = "Add Entry Type",
		notes = "Add or update Study Entry Type")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/entry-types", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity addEntryType(final @PathVariable String cropname,
		@PathVariable final String programUUID,	@RequestBody Enumeration entryType) {
		this.entryTypeService.addEntryType(programUUID, entryType);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Add or update Study Entry Type",
		notes = "Add or update Study Entry Type")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/entry-types", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateEntryType(final @PathVariable String cropname,
		@PathVariable final String programUUID,	@RequestBody Enumeration entryType) {
		this.entryTypeService.updateEntryType(programUUID, entryType);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Delete Study Entry Type", notes = "Delete Study Entry Types")
	@RequestMapping(value = "/{cropname}/programs/{programUUID}/entry-types", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteStudyEntryType(final @PathVariable String cropname,
		@PathVariable final String programUUID, @PathVariable final Integer entryTypeId) {
		this.entryTypeService.deleteEntryType(entryTypeId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
