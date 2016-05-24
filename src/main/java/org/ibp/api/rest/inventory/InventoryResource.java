
package org.ibp.api.rest.inventory;

import java.util.List;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.ibp.api.domain.inventory.GermplasmInventory;
import org.ibp.api.java.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Inventory Services")
@RestController
@RequestMapping("/inventory")
public class InventoryResource {

	@Autowired
	private InventoryService inventoryService;

	@RequestMapping(value = "/{cropname}/germplasm/{gid}", method = RequestMethod.GET)
	@ApiOperation(value = "Get Inventory Information",
			notes = "Returns information about all inventory lots available for the given germplasm id (gid).")
	public List<GermplasmInventory> getInventoryLotInfoForGermplasm(@PathVariable String gid, @PathVariable String cropname) {
		return this.inventoryService.getInventoryLotInfoForGermplasm(gid);
	}

	@RequestMapping(value = "/{cropname}/germplasm/{gid}", method = RequestMethod.PUT)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Created")})
	public ResponseEntity<?> createInverntory(@PathVariable String cropname, @RequestBody GermplasmInventory germplasmInventory,
			@PathVariable String gid) {
		this.inventoryService.createInverntory(germplasmInventory, gid);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@RequestMapping(value = "/{cropname}/germplasm/{gid}", method = RequestMethod.POST)
	public ResponseEntity<?> updateInverntory(@PathVariable String cropname, @RequestBody GermplasmInventory germplasmInventory,
			@PathVariable String gid) {
		this.inventoryService.updateInverntory(germplasmInventory, gid);
		return new ResponseEntity<>(HttpStatus.OK);

	}

	@RequestMapping(value = "/{cropname}/germplasm/{gid}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteInverntory(@PathVariable String cropname, @PathVariable String gid) {
		this.inventoryService.deleteInverntory(gid);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
