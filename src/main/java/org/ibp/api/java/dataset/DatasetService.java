package org.ibp.api.java.dataset;

import org.generationcp.middleware.domain.etl.MeasurementVariable;

import java.util.List;

public interface DatasetService {

	List<MeasurementVariable> getSubObservationSetColumns(Integer subObservationSetId);

	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> traitIds);
	
}
