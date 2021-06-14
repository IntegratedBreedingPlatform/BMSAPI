package org.ibp.api.rest.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.ibp.api.java.germplasm.GermplasmAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "Germplasm Attribute Services")
@Controller
public class GermplasmAttributeResource {

	@Autowired
	private GermplasmAttributeService germplasmAttributeService;

	@ApiOperation(value = "Returns germplasm attributes filtered by gid and attribute type", notes = "Returns germplasm attributes by gid and attribute type")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/attributes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmAttributeDto>> getGermplasmAttributeDtos(@PathVariable final String cropName,
		@PathVariable final Integer gid,
		@RequestParam(required = false) final Integer variableTypeId,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.germplasmAttributeService.getGermplasmAttributeDtos(gid, variableTypeId, programUUID), HttpStatus.OK);
	}

	@ApiOperation(value = "Create attribute for specified germplasm", notes = "Create attribute for specified germplasm")
	@PreAuthorize("hasAnyAuthority('ADMIN','GERMPLASM', 'MANAGE_GERMPLASM', 'EDIT_GERMPLASM', 'MODIFY_ATTRIBUTES')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/attributes", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GermplasmAttributeRequestDto> createGermplasmAttribute(@PathVariable final String cropName, @RequestParam(required = false) final String programUUID,
		@PathVariable final Integer gid, @RequestBody final GermplasmAttributeRequestDto requestDto) {
		return new ResponseEntity<>(this.germplasmAttributeService.createGermplasmAttribute(gid, requestDto, programUUID),
			HttpStatus.OK);
	}

	@ApiOperation(value = "Update germplasm attribute", notes = "Update germplasm attribute")
	@PreAuthorize("hasAnyAuthority('ADMIN','GERMPLASM', 'MANAGE_GERMPLASM', 'EDIT_GERMPLASM', 'MODIFY_ATTRIBUTES')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/attributes/{attributeId}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<GermplasmAttributeRequestDto> updateGermplasmAttribute(@PathVariable final String cropName, @RequestParam(required = false) final String programUUID,
		@PathVariable final Integer gid, @PathVariable final Integer attributeId,
		@ApiParam("Only the following fields can be updated: value, date, and locationId") @RequestBody final GermplasmAttributeRequestDto requestDto) {
		return new ResponseEntity<>(
			this.germplasmAttributeService.updateGermplasmAttribute(gid, attributeId, requestDto, programUUID),
			HttpStatus.OK);
	}

	@ApiOperation(value = "Delete germplasm attribute", notes = "Delete germplasm attribute")
	@PreAuthorize("hasAnyAuthority('ADMIN','GERMPLASM', 'MANAGE_GERMPLASM', 'EDIT_GERMPLASM', 'MODIFY_ATTRIBUTES')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/attributes/{attributeId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Void> deleteGermplasmAttribute(@PathVariable final String cropName, @RequestParam(required = false) final String programUUID,
		@PathVariable final Integer gid, @PathVariable final Integer attributeId) {
		this.germplasmAttributeService.deleteGermplasmAttribute(gid, attributeId);
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
