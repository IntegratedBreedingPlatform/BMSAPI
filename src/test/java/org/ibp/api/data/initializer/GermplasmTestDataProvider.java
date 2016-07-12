package org.ibp.api.data.initializer;

import org.generationcp.middleware.pojos.Germplasm;

public class GermplasmTestDataProvider {

	/**
	 * Method to create single germplasm
	 * @return germplasm
	 */
	public static Germplasm createGermplasm() {
		final Germplasm germplasm = new Germplasm();
		germplasm.setGid(3);
		germplasm.setGpid1(1);
		germplasm.setGpid2(2);
		germplasm.setMethodId(1);
		germplasm.setLocationId(1);

		return germplasm;
	}
}
