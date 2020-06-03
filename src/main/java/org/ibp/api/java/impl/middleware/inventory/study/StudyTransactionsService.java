package org.ibp.api.java.impl.middleware.inventory.study;

import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;

import java.util.List;

public interface StudyTransactionsService {

	long countAllStudyTransactions(Integer studyId, StudyTransactionsRequest studyTransactionsRequest);

	long countFilteredStudyTransactions(Integer studyId, StudyTransactionsRequest studyTransactionsRequest);

	List<StudyTransactionsDto> searchStudyTransactions(final Integer studyId, StudyTransactionsRequest studyTransactionsRequest);
}
