package org.ibp.api.rest.breedingmethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.MethodClassDTO;
import org.generationcp.middleware.pojos.MethodGroup;
import org.generationcp.middleware.pojos.MethodType;
import org.ibp.api.java.breedingmethod.BreedingMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "Breeding methods Services")
@RestController
public class BreedingMethodResourceGroup {

	@Autowired
	private BreedingMethodService breedingMethodService;

	@ApiOperation(value = "Get breeding method")
	@RequestMapping(value = "/crops/{cropName}/breedingmethods/{breedingMethodDbId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BreedingMethodDTO> getBreedingMethod(
		@PathVariable final String cropName,
		@PathVariable final Integer breedingMethodDbId,
		@RequestParam(required = false) final String programUUID
	) {
		return new ResponseEntity<>(this.breedingMethodService.getBreedingMethod(breedingMethodDbId), HttpStatus.OK);
	}

	@ApiOperation(value = "List breeding method types")
	@RequestMapping(value = "/crops/{cropName}/breedingmethod-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<MethodType>> getBreedingMethodTypes(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID
	) {
		return new ResponseEntity<>(MethodType.getAll(), HttpStatus.OK);
	}

	@ApiOperation(value = "List breeding method classes")
	@RequestMapping(value = "/crops/{cropName}/breedingmethod-classes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<MethodClassDTO>> getBreedingMethodClasses(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID
	) {
		return new ResponseEntity<>(this.breedingMethodService.getMethodClasses(), HttpStatus.OK);
	}

	@ApiOperation(value = "List breeding method groups")
	@RequestMapping(value = "/crops/{cropName}/breedingmethod-groups", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<MethodGroup>> getBreedingMethodGroups(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID
	) {
		return new ResponseEntity<>(MethodGroup.getAll(), HttpStatus.OK);
	}

	@ApiOperation(value = "List breeding method filtered by favorites")
	@RequestMapping(value = "/crops/{cropName}/breedingmethod-favorites", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<BreedingMethodDTO>> getBreedingMethodFavorites(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestParam final boolean favoriteMethods
	) {
		final List<BreedingMethodDTO> breedingMethods = this.breedingMethodService.getBreedingMethods(cropName, programUUID,
			favoriteMethods);
		return new ResponseEntity<>(breedingMethods, HttpStatus.OK);
	}

}
