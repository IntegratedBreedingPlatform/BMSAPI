package org.ibp.api.java.dataset;

import org.ibp.api.java.impl.middleware.dataset.DatasetCollectionOrderServiceImpl;
import org.ibp.api.rest.dataset.ObservationUnitRow;

import java.util.List;

public interface DatasetCollectionOrderService {

	List<ObservationUnitRow> reorder(
		DatasetCollectionOrderServiceImpl.CollectionOrder collectionOrder,
		int trialDatasetId, String instanceNumber, List<ObservationUnitRow> observationUnitRows);
}
