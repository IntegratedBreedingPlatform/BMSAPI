package org.ibp.api.rest.inventory.planting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.inventory.planting.PlantingMetadata;
import org.generationcp.middleware.domain.inventory.planting.PlantingRequestDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.ibp.api.java.inventory.planting.PlantingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Planting Services")
@RestController
public class PlantingResource {

	@Autowired
	private PlantingService plantingService;

	private static final String HAS_PLANTING_PERMISSIONS =
		"hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES', 'MS_MANAGE_OBSERVATION_UNITS' , 'MS_WITHDRAW_INVENTORY')";

	@ApiOperation(value = "Get existing planting information for selected observation units", notes = "Get existing planting information for selected observation units")
	@RequestMapping(value = "/crops/{cropName}/planting/metadata", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize(HAS_PLANTING_PERMISSIONS + " or hasAnyAuthority('MS_CREATE_PENDING_WITHDRAWALS', 'MS_CREATE_CONFIRMED_WITHDRAWALS')")
	public ResponseEntity<PlantingMetadata> getPlantingMetadata(
		@PathVariable final String cropName, //
		@ApiParam("Planting Instructions")
		@RequestBody final PlantingRequestDto plantingRequestDto) {
		return new ResponseEntity<>(plantingService.getPlantingMetadata(plantingRequestDto), HttpStatus.OK);
	}

	@ApiOperation(value = "Generate planting for the selected observation units", notes = "Generate planting for the selected observation units")
	@RequestMapping(value = "/crops/{cropName}/planting/pending-generation", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize(HAS_PLANTING_PERMISSIONS + " or hasAnyAuthority('MS_CREATE_PENDING_WITHDRAWALS')")
	public ResponseEntity<Void> postPendingPlanting(
		@PathVariable final String cropName, //
		@ApiParam("Planting Instructions")
		@RequestBody final PlantingRequestDto plantingRequestDto) {
		plantingService.generatePlanting(plantingRequestDto, TransactionStatus.PENDING);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Generate planting for the selected observation units", notes = "Generate planting for the selected observation units")
	@RequestMapping(value = "/crops/{cropName}/planting/confirmed-generation", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize(HAS_PLANTING_PERMISSIONS + " or hasAnyAuthority('MS_CREATE_CONFIRMED_WITHDRAWALS')")
	public ResponseEntity<Void> postConfirmedPlanting(
		@PathVariable final String cropName, //
		@ApiParam("Planting Instructions")
		@RequestBody final PlantingRequestDto plantingRequestDto) {
		plantingService.generatePlanting(plantingRequestDto, TransactionStatus.CONFIRMED);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
