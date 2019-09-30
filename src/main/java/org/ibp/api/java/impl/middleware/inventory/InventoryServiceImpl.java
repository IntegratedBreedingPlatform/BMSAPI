
package org.ibp.api.java.impl.middleware.inventory;

import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
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
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Autowired
	private LocationDataManager locationDataManager;

	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);

	@Override
	public List<GermplasmInventory> getInventoryLotInfoForGermplasm(final String germplasmId) {

		final List<GermplasmInventory> germplasmInventory = new ArrayList<GermplasmInventory>();
		try {
			final List<LotDetails> result = this.inventoryDataManager.getLotDetailsForGermplasm(Integer.valueOf(germplasmId));
			if (result != null) {
				for (final LotDetails lotDetail : result) {
					final GermplasmInventory gpInverntory = new GermplasmInventory(lotDetail.getEntityIdOfLot());
					gpInverntory.setLotId(lotDetail.getLotId());
					gpInverntory.setQuantityAvailable(lotDetail.getAvailableLotBalance());
					gpInverntory.setQuantityReserved(lotDetail.getReservedTotal());
					gpInverntory.setQuantityTotal(lotDetail.getAvailableLotBalance() + lotDetail.getReservedTotal());

					final TermSummary quantityUnit = new TermSummary();
					quantityUnit.setId(String.valueOf(lotDetail.getScaleOfLot().getId()));
					quantityUnit.setName(lotDetail.getScaleOfLot().getName());
					quantityUnit.setDescription(lotDetail.getScaleOfLot().getDefinition());

					gpInverntory.setQuantityUnit(quantityUnit);
					gpInverntory.setComments(lotDetail.getCommentOfLot());

					final InventoryLocation locationOfLot =
							new InventoryLocation(lotDetail.getLocationOfLot().getLocid(), lotDetail.getLocationOfLot().getLname() + " ("
									+ lotDetail.getLocationOfLot().getLabbr() + ")");

					final Location l1 = this.locationDataManager.getLocationByID(lotDetail.getLocationOfLot().getSnl1id());
					final Location l2 = this.locationDataManager.getLocationByID(lotDetail.getLocationOfLot().getSnl2id());
					final Location l3 = this.locationDataManager.getLocationByID(lotDetail.getLocationOfLot().getSnl3id());

					locationOfLot.setLabel1(l1 != null ? l1.getLname() + " (" + l1.getLabbr() + ")" : "");
					locationOfLot.setLabel2(l2 != null ? l2.getLname() + " (" + l2.getLabbr() + ")" : "");
					locationOfLot.setLabel3(l3 != null ? l3.getLname() + " (" + l3.getLabbr() + ")" : "");

					gpInverntory.setLocation(locationOfLot);

					// TODO Fields not available in LotDetails are hard coded for now. Good enough for demo.
					gpInverntory.setLotStatus(LotStatus.ACTIVE);
					gpInverntory.setUserId(1);
					gpInverntory.setUserName("Mr. Plant Breeder");
					germplasmInventory.add(gpInverntory);
				}
			}
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
		return germplasmInventory;
	}

	@Override
	public void createInverntory(final GermplasmInventory germplasmInventory, final String germplasmId) {
		InventoryServiceImpl.LOGGER.debug(germplasmInventory.toString());

		try {
			final Lot lot = new Lot();
			lot.setUserId(germplasmInventory.getUserId());
			lot.setEntityType(EntityType.GERMPLSM.name());
			lot.setEntityId(Integer.valueOf(germplasmId));
			lot.setLocationId(germplasmInventory.getLocation().getId());
			lot.setScaleId(Integer.valueOf(germplasmInventory.getQuantityUnit().getId()));
			lot.setComments(germplasmInventory.getComments());
			lot.setStatus(LotStatus.ACTIVE.getIntValue());
			this.inventoryDataManager.addLot(lot);
			InventoryServiceImpl.LOGGER.debug("Lot created: LotId: " + lot.getId());

			final Transaction trans = new Transaction();
			trans.setLot(lot);
			trans.setUserId(germplasmInventory.getUserId());
			trans.setTransactionDate(InventoryServiceImpl.getCurrentDate());
			trans.setStatus(0);
			trans.setQuantity(germplasmInventory.getQuantityTotal());
			trans.setComments(germplasmInventory.getComments());
			trans.setSourceType("?");
			trans.setCommitmentDate(0);
			trans.setPreviousAmount(Double.valueOf(0));
			this.inventoryDataManager.addTransaction(trans);
			InventoryServiceImpl.LOGGER.debug("Transaction created: TransactionId: " + trans.getId());

		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	private static Integer getCurrentDateInt() {
		return Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()));
	}

	private static Date getCurrentDate() {
		return Calendar.getInstance().getTime();
	}

	@Override
	public void updateInverntory(final GermplasmInventory germplasmInventory, final String germplasmId) {
		InventoryServiceImpl.LOGGER.debug(germplasmId);
		InventoryServiceImpl.LOGGER.debug(germplasmInventory.toString());
		throw new UnsupportedOperationException("This operation has not yet been implemented.");
	}

	@Override
	public void deleteInverntory(final String germplasmId) {
		InventoryServiceImpl.LOGGER.debug(germplasmId);
		throw new UnsupportedOperationException("This operation has not yet been implemented.");
	}

}
