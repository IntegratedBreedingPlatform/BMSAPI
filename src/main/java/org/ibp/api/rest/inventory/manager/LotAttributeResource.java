package org.ibp.api.rest.inventory.manager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.shared.AttributeRequestDto;
import org.generationcp.middleware.domain.shared.RecordAttributeDto;
import org.ibp.api.java.inventory.manager.LotAttributeService;
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

@Api(value = "Lot Attribute Services")
@Controller
public class LotAttributeResource {

	@Autowired
	private LotAttributeService lotAttributeService;

	private static final String HAS_MANAGE_LOTS = "hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_INVENTORY', 'MANAGE_LOTS')";

	@ApiOperation(value = "Returns lot attributes filtered by lot ID", notes = "Returns lot attributes by lot ID")
	@RequestMapping(value = "/crops/{cropName}/lot/{lotId}/attributes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<RecordAttributeDto>> getLotAttributeDtos(@PathVariable final String cropName,
		@PathVariable final Integer lotId,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.lotAttributeService.getLotAttributeDtos(lotId, programUUID), HttpStatus.OK);
	}

	@ApiOperation(value = "Create attribute for specified lot", notes = "Create attribute for specified lot")
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('UPDATE_LOTS')")
	@RequestMapping(value = "/crops/{cropName}/lot/{lotId}/attributes", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<AttributeRequestDto> createLotAttribute(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@PathVariable final Integer lotId, @RequestBody final AttributeRequestDto requestDto) {
		return new ResponseEntity<>(this.lotAttributeService.createLotAttribute(lotId, requestDto),
			HttpStatus.OK);
	}

	@ApiOperation(value = "Update lot attribute", notes = "Update lot attribute")
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('UPDATE_LOTS')")
	@RequestMapping(value = "/crops/{cropName}/lot/{lotId}/attributes/{attributeId}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<AttributeRequestDto> updateLotAttribute(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@PathVariable final Integer lotId, @PathVariable final Integer attributeId,
		@ApiParam("Only the following fields can be updated: value, date, and locationId") @RequestBody
		final AttributeRequestDto requestDto) {
		return new ResponseEntity<>(
			this.lotAttributeService.updateLotAttribute(lotId, attributeId, requestDto),
			HttpStatus.OK);
	}

	@ApiOperation(value = "Delete lot attribute", notes = "Delete lot attribute")
	@PreAuthorize(HAS_MANAGE_LOTS + " or hasAnyAuthority('UPDATE_LOTS')")
	@RequestMapping(value = "/crops/{cropName}/lot/{lotId}/attributes/{attributeId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Void> deleteLotAttribute(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@PathVariable final Integer lotId, @PathVariable final Integer attributeId) {
		this.lotAttributeService.deleteLotAttribute(lotId, attributeId);
		return new ResponseEntity<>(HttpStatus.OK);

	}

}
