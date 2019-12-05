
package org.ibp.api.java.inventory;

import java.util.List;

import org.ibp.api.domain.inventory.GermplasmInventory;

public interface InventoryService {

	List<GermplasmInventory> getInventoryLotInfoForGermplasm(String germplasmId);

	void createInverntory(GermplasmInventory germplasmInventory, String germplasmId);

	void updateInverntory(GermplasmInventory germplasmInventory, String germplasmId);

	void deleteInverntory(String germplasmId);

}
