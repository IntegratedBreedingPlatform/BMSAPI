package org.ibp.api.java.study;

import org.generationcp.middleware.service.api.dataset.StockPropertyData;

public interface StudyEntryObservationService {

	Integer createObservation(String programUUID, Integer studyId, final Integer datasetId, StockPropertyData stockPropertyData);

//	void update(String programUUID, Integer listId, Integer observationId, String value);
//
//	void delete(Integer listId, Integer observationId);
//
//	long countObservationsByVariables(Integer listId, List<Integer> variableIds);
}
