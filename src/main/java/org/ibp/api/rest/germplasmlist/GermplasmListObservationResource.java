package org.ibp.api.rest.germplasmlist;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.germplasmlist.GermplasmListObservationRequestDto;
import org.ibp.api.java.germplasm.GermplasmListObservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

@Api(value = "Germplasm List Observation Services")
@Controller
public class GermplasmListObservationResource {

	@Autowired
	public GermplasmListObservationService germplasmListObservationService;

	@ApiOperation(value = "Add new observation to the germplasm list", notes = "Add new observation to the germplasm list")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'LISTS', 'MANAGE_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/observations", method = RequestMethod.PUT)
	public ResponseEntity<Integer> createGermplasmListObservation(
		@PathVariable final String cropName, @PathVariable final Integer listId, @RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmListObservationRequestDto germplasmListObservationRequestDto) {
		return new ResponseEntity<>(this.germplasmListObservationService.create(listId, germplasmListObservationRequestDto), HttpStatus.OK);
	}

	@ApiOperation(value = "Remove observation from the germplasm list", notes = "Remove observation from the germplasm list")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'LISTS', 'MANAGE_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/observations/{observationId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeGermplasmListObservation(
		@PathVariable final String cropName, @PathVariable final Integer listId,
		@PathVariable(value = "observationId") final Integer observationId,
		@RequestParam(required = false) final String programUUID) {
		this.germplasmListObservationService.delete(listId, observationId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get the variables associated to the list filtered by variableType", notes = "Get the list variables filtered by variableType")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'LISTS', 'MANAGE_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/observations/{observationId}", method = RequestMethod.PATCH)
	public ResponseEntity<Void> modifyGermplasmListObservation(
		@PathVariable final String cropName, @PathVariable final Integer listId, @PathVariable final Integer observationId,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final String value) {

		this.germplasmListObservationService.update(listId, observationId, value);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Count Germplasm List Observations", notes = "Returns count of germplasm list observations given a set of variables")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'LISTS', 'MANAGE_GERMPLASM_LISTS')")
	@RequestMapping(value = "/crops/{cropName}/germplasm-lists/{listId}/variables/observations", method = RequestMethod.HEAD)
	public ResponseEntity<Void> countObservationsByVariables(
		@PathVariable final String cropName, @PathVariable final Integer listId,
		@RequestParam(required = false) final String programUUID, @RequestParam(value = "variableIds") final Integer[] variableIds) {

		final long count = this.germplasmListObservationService.countObservationsByVariables(listId, Arrays.asList(variableIds));
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>(respHeaders, HttpStatus.OK);
	}

}
