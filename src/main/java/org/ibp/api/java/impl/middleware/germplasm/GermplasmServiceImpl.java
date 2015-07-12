
package org.ibp.api.java.impl.middleware.germplasm;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GermplasmServiceImpl implements GermplasmService {

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private LocationDataManager locationDataManger;

	@Override
	public List<GermplasmSummary> searchGermplasm(String searchText) {
		List<GermplasmSummary> results = new ArrayList<GermplasmSummary>();
		try {
			List<Germplasm> searchResults = this.germplasmDataManager.searchForGermplasm(searchText, Operation.LIKE, false, false);
			for (Germplasm germplasm : searchResults) {
				results.add(this.populateGermplasmSummary(germplasm));
			}
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
		return results;
	}

	private GermplasmSummary populateGermplasmSummary(Germplasm germplasm) throws MiddlewareQueryException {
		if (germplasm == null) {
			return null;
		}
		GermplasmSummary summary = new GermplasmSummary();
		summary.setGermplasmId(germplasm.getGid().toString());
		summary.setParent1Id(germplasm.getGpid1() != null && germplasm.getGpid1() != 0 ? germplasm.getGpid1().toString() : "Unknown");
		summary.setParent2Id(germplasm.getGpid2() != null && germplasm.getGpid2() != 0 ? germplasm.getGpid2().toString() : "Unknown");

		CrossExpansionProperties crossExpansionProperties = new CrossExpansionProperties();
		crossExpansionProperties.setDefaultLevel(1);
		crossExpansionProperties.setWheatLevel(1);
		summary.setPedigreeString(this.pedigreeService.getCrossExpansion(germplasm.getGid(), crossExpansionProperties));

		// FIXME - select in a loop ... Middleware service should handle all this in main query.
		List<Name> namesByGID = this.germplasmDataManager.getNamesByGID(new Integer(germplasm.getGid()), null, null);
		List<String> names = new ArrayList<String>();
		for (Name gpName : namesByGID) {
			names.add(gpName.getNval());
		}
		summary.addNames(names);

		Method germplasmMethod = this.germplasmDataManager.getMethodByID(germplasm.getMethodId());
		if (germplasmMethod != null && germplasmMethod.getMname() != null) {
			summary.setBreedingMethod(germplasmMethod.getMname());
		}

		Location germplasmLocation = this.locationDataManger.getLocationByID(germplasm.getLocationId());
		if (germplasmLocation != null && germplasmLocation.getLname() != null) {
			summary.setLocation(germplasmLocation.getLname());
		}
		return summary;
	}

	@Override
	public GermplasmSummary getGermplasm(String germplasmId) {
		Germplasm germplasm;
		try {
			germplasm = this.germplasmDataManager.getGermplasmByGID(Integer.valueOf(germplasmId));
			return this.populateGermplasmSummary(germplasm);
		} catch (NumberFormatException | MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	void setPedigreeService(PedigreeService pedigreeService) {
		this.pedigreeService = pedigreeService;
	}

	void setLocationDataManger(LocationDataManager locationDataManger) {
		this.locationDataManger = locationDataManger;
	}
}
