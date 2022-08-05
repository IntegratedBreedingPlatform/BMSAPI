package org.ibp.api.rest.inventory.planting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.planting.PlantingMetadata;
import org.generationcp.middleware.domain.inventory.planting.PlantingRequestDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.impl.inventory.PlantingPreparationDTO;
import org.ibp.api.java.impl.middleware.inventory.common.InventoryLock;
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
import springfox.documentation.annotations.ApiIgnore;

@Api(value = "Planting Services")
@RestController
public class PlantingResource {

	@Autowired
	private PlantingService plantingService;

	@Autowired
	private InventoryLock inventoryLock;

	private static final String HAS_PLANTING_PERMISSIONS =
		"hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES', 'MS_MANAGE_OBSERVATION_UNITS' , 'MS_WITHDRAW_INVENTORY')";

	@ApiIgnore
	@ApiOperation(value = "Planting preparation search", notes = "Planting search returns data necessary to prepare planting")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/planting/preparation/search", method = RequestMethod.POST)
	@PreAuthorize(HAS_PLANTING_PERMISSIONS + " or hasAnyAuthority('MS_CREATE_PENDING_WITHDRAWALS', 'MS_CREATE_CONFIRMED_WITHDRAWALS')")
	@ResponseBody
	public ResponseEntity<PlantingPreparationDTO> searchPlanting(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@RequestBody final SearchCompositeDto<ObservationUnitsSearchDTO, Integer> searchCompositeDto
	) {
		final PlantingPreparationDTO plantingPreparationDTO =
			plantingService.searchPlantingPreparation(studyId, datasetId, searchCompositeDto);

		return new ResponseEntity<>(plantingPreparationDTO, HttpStatus.OK);
	}

	@ApiIgnore
	@ApiOperation(value = "Get existing planting information for selected observation units", notes = "Get existing planting information for selected observation units")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/planting/metadata", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize(HAS_PLANTING_PERMISSIONS + " or hasAnyAuthority('MS_CREATE_PENDING_WITHDRAWALS', 'MS_CREATE_CONFIRMED_WITHDRAWALS')")
	public ResponseEntity<PlantingMetadata> getPlantingMetadata(
		@PathVariable final String cropName, //
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@ApiParam("Planting Instructions")
		@RequestBody final PlantingRequestDto plantingRequestDto) {
		return new ResponseEntity<>(plantingService.getPlantingMetadata(studyId, datasetId, plantingRequestDto), HttpStatus.OK);
	}


	@ApiIgnore
	@ApiOperation(value = "Generate pending planting for the selected observation units", notes = "Generate pending planting for the selected observation units")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/planting/pending-generation", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize(HAS_PLANTING_PERMISSIONS + " or hasAnyAuthority('MS_CREATE_PENDING_WITHDRAWALS')")
	public ResponseEntity<Void> postPendingPlanting(
		@PathVariable final String cropName, //
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@ApiParam("Planting Instructions")
		@RequestBody final PlantingRequestDto plantingRequestDto) {
		try {
			inventoryLock.lockWrite();
			plantingService.generatePlanting(studyId, datasetId, plantingRequestDto, TransactionStatus.PENDING);
			return new ResponseEntity<>(HttpStatus.OK);
		} finally {
			inventoryLock.unlockWrite();
		}
	}

	@ApiIgnore
	@ApiOperation(value = "Generate confirmed planting for the selected observation units", notes = "Generate confirmed planting for the selected observation units")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/datasets/{datasetId}/planting/confirmed-generation", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize(HAS_PLANTING_PERMISSIONS + " or hasAnyAuthority('MS_CREATE_CONFIRMED_WITHDRAWALS')")
	public ResponseEntity<Void> postConfirmedPlanting(
		@PathVariable final String cropName, //
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@ApiParam("Planting Instructions")
		@RequestBody final PlantingRequestDto plantingRequestDto) {
		try {
			inventoryLock.lockWrite();
			plantingService.generatePlanting(studyId, datasetId, plantingRequestDto, TransactionStatus.CONFIRMED);
			return new ResponseEntity<>(HttpStatus.OK);
		} finally {
			inventoryLock.unlockWrite();
		}
	}
}
