package org.generationcp.bms.resource;

import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.bms.domain.GermplasmMarkerInfo;
import org.generationcp.bms.domain.MarkerCount;
import org.generationcp.bms.exception.MissingRequiredParameterException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/genotype")
public class GenotypeResource {

	@Autowired
	private GenotypicDataManager genotypicDataManager;
	
	@Autowired
	private SimpleDao simpleDao;

	@RequestMapping(value = "/{gid}/count", method = RequestMethod.GET)
	@ResponseBody
	public GermplasmMarkerInfo countMarkers(@PathVariable Integer gid, @RequestParam(required = false) String method)
			throws MissingRequiredParameterException, MiddlewareQueryException {

		if (gid == null) {
			throw new MissingRequiredParameterException("gid", "Number");
		}
		
		GermplasmMarkerInfo result = new GermplasmMarkerInfo(gid); 
		List<Integer> datasetIds = simpleDao.getAllGDMSDatasetIDs();
		
		// We are assuming a dataset equates to what is referred to as a "run"
		
		for (Integer datasetId : datasetIds) {
			long count = genotypicDataManager.countMarkersByGidAndDatasetIds(gid, Arrays.asList(datasetId));
			Dataset datasetDetail = genotypicDataManager.getDatasetById(datasetId);			
			result.addMarkerCount(new MarkerCount(datasetId, datasetDetail.getMethod(), count));
		}
	
		return result;
	}
	
	
	@RequestMapping(value = "/{gid}", method = RequestMethod.GET)
	public String getGenotypeData(@PathVariable Integer gid,
			@RequestParam(required = false) Integer runId,
			@RequestParam(required = false) String method,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) Integer pageNumber) {

		
		return "TODO";
	}

}
