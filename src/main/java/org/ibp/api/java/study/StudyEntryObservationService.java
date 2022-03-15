package org.ibp.api.java.study;

import org.generationcp.middleware.service.api.dataset.StockPropertyData;

public interface StudyEntryObservationService {

	Integer createObservation(Integer studyId, StockPropertyData stockPropertyData);

	Integer updateObservation(Integer studyId, StockPropertyData stockPropertyData);

	void deleteObservation(Integer studyId, Integer stockPropertyId);

}
