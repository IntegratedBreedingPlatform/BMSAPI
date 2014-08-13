package org.generationcp.bms.resource;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.bms.domain.GermplasmInventoryInfo;
import org.generationcp.bms.domain.LocationInfo;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryResource {
	
	@Autowired
	private InventoryDataManager inventoryDataManager;
	
	@Autowired
	private LocationDataManager locationDataManager;
	
	@Autowired
	private UserDataManager userDataManager;
	
	@RequestMapping(value = "/germplasm/{gid}", method = RequestMethod.GET)
	public List<GermplasmInventoryInfo> getInventoryLotInfoForGermplasm(@PathVariable Integer gid) throws MiddlewareQueryException {
		
		List<GermplasmInventoryInfo> germplasmInventoryInfo = new ArrayList<GermplasmInventoryInfo>();
		
		List<LotDetails> result = inventoryDataManager.getLotDetailsForGermplasm(gid);
		if(result != null) {
			
			for(LotDetails lotDetail : result) {
				
				GermplasmInventoryInfo gpInverntory = new GermplasmInventoryInfo(lotDetail.getEntityIdOfLot());
				gpInverntory.setLotId(lotDetail.getLotId());
				gpInverntory.setQuantityAvailable(lotDetail.getAvailableLotBalance());
				gpInverntory.setQuantityReserved(lotDetail.getReservedTotal());
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
				
				//lotStatus
				//lotUser
				
				germplasmInventoryInfo.add(gpInverntory);
				
			}
		
		}
		
		return germplasmInventoryInfo;
	}

}
