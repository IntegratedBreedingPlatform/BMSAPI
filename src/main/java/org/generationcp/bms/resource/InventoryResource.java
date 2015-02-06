package org.generationcp.bms.resource;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.generationcp.bms.domain.GermplasmInventoryInfo;
import org.generationcp.bms.domain.InventoryOperationResponse;
import org.generationcp.bms.domain.LocationInfo;
import org.generationcp.bms.domain.TermSummary;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.ims.EntityType;
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryResource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryResource.class);
	
	@Autowired
	private InventoryDataManager inventoryDataManager;
	
	@Autowired
	private LocationDataManager locationDataManager;
	
	@Autowired
	private UserDataManager userDataManager;
	
	@RequestMapping(value = "/germplasm/{gid}", method = RequestMethod.GET)
	@ApiOperation(value = "Get Inventory Information", notes = "Returns information about all inventory lots available for the given germplasm id (gid).")
	public List<GermplasmInventoryInfo> getInventoryLotInfoForGermplasm(@PathVariable Integer gid)
			throws MiddlewareQueryException {
		
		List<GermplasmInventoryInfo> germplasmInventoryInfo = new ArrayList<GermplasmInventoryInfo>();
		
		List<LotDetails> result = inventoryDataManager.getLotDetailsForGermplasm(gid);
		if(result != null) {
			
			for(LotDetails lotDetail : result) {
				
				GermplasmInventoryInfo gpInverntory = new GermplasmInventoryInfo(lotDetail.getEntityIdOfLot());
				gpInverntory.setLotId(lotDetail.getLotId());
				gpInverntory.setQuantityAvailable(lotDetail.getAvailableLotBalance());
				gpInverntory.setQuantityReserved(lotDetail.getReservedTotal());
				gpInverntory.setQuantityTotal(lotDetail.getAvailableLotBalance() + lotDetail.getReservedTotal());
				gpInverntory.setQuantityUnit(new TermSummary(lotDetail.getScaleOfLot().getId(), lotDetail.getScaleOfLot().getName(), lotDetail.getScaleOfLot().getDefinition()));
				gpInverntory.setComments(lotDetail.getCommentOfLot());
				
				LocationInfo locationOfLot = new LocationInfo(lotDetail.getLocationOfLot().getLocid(), 
						lotDetail.getLocationOfLot().getLname() + " (" + lotDetail.getLocationOfLot().getLabbr() + ")");
				
				Location l1 = locationDataManager.getLocationByID(lotDetail.getLocationOfLot().getSnl1id());
				Location l2 = locationDataManager.getLocationByID(lotDetail.getLocationOfLot().getSnl2id());
				Location l3 = locationDataManager.getLocationByID(lotDetail.getLocationOfLot().getSnl3id());
				
				locationOfLot.setLabel1(l1 != null ? (l1.getLname() + " (" + l1.getLabbr() + ")") : "");
				locationOfLot.setLabel2(l2 != null ? (l2.getLname() + " (" + l2.getLabbr() + ")") : "");
				locationOfLot.setLabel3(l3 != null ? (l3.getLname() + " (" + l3.getLabbr() + ")") : "");
				
				gpInverntory.setLocation(locationOfLot);
				
				//TODO Fields not available in LotDetails are hard coded for now. Good enough for demo.
				gpInverntory.setLotStatus(LotStatus.ACTIVE);
				gpInverntory.setUserId(-1);
				gpInverntory.setUserName("Mr. Plant Breeder");
				
				germplasmInventoryInfo.add(gpInverntory);			
			}
		
		}
		
		return germplasmInventoryInfo;
	}
	
	@RequestMapping(value = "/germplasm/{gid}", method = RequestMethod.PUT)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created")})
	public ResponseEntity<InventoryOperationResponse> createInverntory(
			@RequestBody GermplasmInventoryInfo inventoryInfo, @PathVariable Integer gid)
			throws MiddlewareQueryException {
		
		LOGGER.debug(inventoryInfo.toString());
		
		Lot lot = new Lot();
		lot.setUserId(inventoryInfo.getUserId());
		lot.setEntityType(EntityType.GERMPLSM.name());
		lot.setEntityId(gid);
		lot.setLocationId(inventoryInfo.getLocation().getId());
		lot.setScaleId(inventoryInfo.getQuantityUnit().getId());
		lot.setComments(inventoryInfo.getComments());		
		lot.setStatus(LotStatus.ACTIVE.getIntValue());
		Integer lotId = inventoryDataManager.addLot(lot);
		
		Transaction trans = new Transaction();
		trans.setLot(lot);
		trans.setUserId(inventoryInfo.getUserId());
		trans.setTransactionDate(getCurrentDateInt());
		trans.setStatus(0);
		trans.setQuantity(inventoryInfo.getQuantityTotal());
		trans.setComments(inventoryInfo.getComments());
		trans.setSourceType("?");
		trans.setPersonId(-1);
		trans.setCommitmentDate(0);
		trans.setPreviousAmount(Double.valueOf(0));
		Integer transId = inventoryDataManager.addTransaction(trans);
		
		InventoryOperationResponse response = new InventoryOperationResponse();
		response.setMessage("Inventory lot created successfully.");
		response.setLotId(lotId);
		response.setTransactionId(transId);
				
		return new ResponseEntity<InventoryOperationResponse>(response, HttpStatus.CREATED);		
	}
	
    private static Integer getCurrentDateInt(){
        return Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()));
    }
	
	@RequestMapping(value = "/germplasm/{gid}", method = RequestMethod.POST)
	public String updateInverntory(@RequestBody GermplasmInventoryInfo inventoryInfo, @PathVariable Integer gid) {
		LOGGER.debug(inventoryInfo.toString());
		return "\"This operation has not yet been implemented.\"";
		
	}
	
	@RequestMapping(value = "/germplasm/{gid}", method = RequestMethod.DELETE)
	public String deleteInverntory(@PathVariable Integer gid) {
		LOGGER.debug(gid.toString());
		return "\"This operation has not yet been implemented.\"";
	}

}
