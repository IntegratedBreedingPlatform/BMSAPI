package org.ibp.api.java.dataset;

import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.impl.middleware.dataset.DatasetCollectionOrderServiceImpl;
import org.ibp.api.rest.dataset.ObservationUnitRow;

import java.util.List;
import java.util.Map;

public interface DatasetCollectionOrderService {

	void reorder(
		DatasetCollectionOrderServiceImpl.CollectionOrder collectionOrder,
		int trialDatasetId, Map<Integer, StudyInstance>  selectedDatasetInstancesMap, Map<Integer, List<ObservationUnitRow>> observationUnitRowMap);
}
