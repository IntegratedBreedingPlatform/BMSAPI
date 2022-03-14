package org.ibp.api.java.study;

import org.generationcp.middleware.service.api.dataset.StockPropertyData;

public interface StudyEntryObservationService {

	Integer createObservation(Integer studyId, Integer datasetId, StockPropertyData stockPropertyData);

	Integer updateObservation(Integer studyId, final Integer datasetId, StockPropertyData stockPropertyData);

	void deleteObservation(Integer studyId, Integer datasetId, Integer stockPropertyId);

}
