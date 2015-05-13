
package org.ibp.api.java.impl.middleware.inventory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import org.ibp.api.domain.inventory.GermplasmInventory;
import org.ibp.api.domain.inventory.InventoryLocation;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.Inventory.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements InventoryService {

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private UserDataManager userDataManager;

	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);

	@Override
	public List<GermplasmInventory> getInventoryLotInfoForGermplasm(String germplasmId) {

		List<GermplasmInventory> germplasmInventory = new ArrayList<GermplasmInventory>();
		try {
			List<LotDetails> result = inventoryDataManager.getLotDetailsForGermplasm(Integer.valueOf(germplasmId));
			if (result != null) {
				for (LotDetails lotDetail : result) {
					GermplasmInventory gpInverntory = new GermplasmInventory(lotDetail.getEntityIdOfLot());
					gpInverntory.setLotId(lotDetail.getLotId());
					gpInverntory.setQuantityAvailable(lotDetail.getAvailableLotBalance());
					gpInverntory.setQuantityReserved(lotDetail.getReservedTotal());
					gpInverntory.setQuantityTotal(lotDetail.getAvailableLotBalance() + lotDetail.getReservedTotal());
					
					TermSummary quantityUnit = new TermSummary();
					quantityUnit.setId(String.valueOf(lotDetail.getScaleOfLot().getId()));
					quantityUnit.setName(lotDetail.getScaleOfLot().getName());
					quantityUnit.setDescription(lotDetail.getScaleOfLot().getDefinition());
					
					gpInverntory.setQuantityUnit(quantityUnit);
					gpInverntory.setComments(lotDetail.getCommentOfLot());

					InventoryLocation locationOfLot =
							new InventoryLocation(lotDetail.getLocationOfLot().getLocid(), lotDetail.getLocationOfLot().getLname() + " (" + lotDetail.getLocationOfLot().getLabbr()
									+ ")");

					Location l1 = locationDataManager.getLocationByID(lotDetail.getLocationOfLot().getSnl1id());
					Location l2 = locationDataManager.getLocationByID(lotDetail.getLocationOfLot().getSnl2id());
					Location l3 = locationDataManager.getLocationByID(lotDetail.getLocationOfLot().getSnl3id());

					locationOfLot.setLabel1(l1 != null ? (l1.getLname() + " (" + l1.getLabbr() + ")") : "");
					locationOfLot.setLabel2(l2 != null ? (l2.getLname() + " (" + l2.getLabbr() + ")") : "");
					locationOfLot.setLabel3(l3 != null ? (l3.getLname() + " (" + l3.getLabbr() + ")") : "");

					gpInverntory.setLocation(locationOfLot);

					// TODO Fields not available in LotDetails are hard coded for now. Good enough for demo.
					gpInverntory.setLotStatus(LotStatus.ACTIVE);
					gpInverntory.setUserId(1);
					gpInverntory.setUserName("Mr. Plant Breeder");
					germplasmInventory.add(gpInverntory);
				}
			}
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
		return germplasmInventory;
	}

	@Override
	public void createInverntory(GermplasmInventory germplasmInventory, String germplasmId) {
		LOGGER.debug(germplasmInventory.toString());

		try {
			Lot lot = new Lot();
			lot.setUserId(germplasmInventory.getUserId());
			lot.setEntityType(EntityType.GERMPLSM.name());
			lot.setEntityId(Integer.valueOf(germplasmId));
			lot.setLocationId(germplasmInventory.getLocation().getId());
			lot.setScaleId(Integer.valueOf(germplasmInventory.getQuantityUnit().getId()));
			lot.setComments(germplasmInventory.getComments());
			lot.setStatus(LotStatus.ACTIVE.getIntValue());
			inventoryDataManager.addLot(lot);
			LOGGER.debug("Lot created: LotId: " + lot.getId());

			Transaction trans = new Transaction();
			trans.setLot(lot);
			trans.setUserId(germplasmInventory.getUserId());
			trans.setTransactionDate(getCurrentDateInt());
			trans.setStatus(0);
			trans.setQuantity(germplasmInventory.getQuantityTotal());
			trans.setComments(germplasmInventory.getComments());
			trans.setSourceType("?");
			trans.setPersonId(1);
			trans.setCommitmentDate(0);
			trans.setPreviousAmount(Double.valueOf(0));
			inventoryDataManager.addTransaction(trans);
			LOGGER.debug("Transaction created: TransactionId: " + trans.getId());
			
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	private static Integer getCurrentDateInt() {
		return Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()));
	}

	@Override
	public void updateInverntory(GermplasmInventory germplasmInventory, String germplasmId) {
		LOGGER.debug(germplasmId);
		LOGGER.debug(germplasmInventory.toString());
		throw new UnsupportedOperationException("This operation has not yet been implemented.");
	}

	@Override
	public void deleteInverntory(String germplasmId) {
		LOGGER.debug(germplasmId);
		throw new UnsupportedOperationException("This operation has not yet been implemented.");
	}

}
