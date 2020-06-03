package org.ibp.api.java.impl.middleware.inventory.study;

import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudyTransactionsServiceImpl implements StudyTransactionsService {

	@Autowired
	private org.generationcp.middleware.api.inventory.study.StudyTransactionsService studyTransactionsService;

	@Override
	public long countAllStudyTransactions(final Integer studyId, final StudyTransactionsRequest studyTransactionsRequest) {
		return this.studyTransactionsService.countAllStudyTransactions(studyId, studyTransactionsRequest);
	}

	@Override
	public long countFilteredStudyTransactions(final Integer studyId, final StudyTransactionsRequest studyTransactionsRequest) {
		return studyTransactionsService.countFilteredStudyTransactions(studyId, studyTransactionsRequest);
	}

	@Override
	public List<StudyTransactionsDto> searchStudyTransactions(final Integer studyId,
		final StudyTransactionsRequest studyTransactionsRequest) {

		return this.studyTransactionsService.searchStudyTransactions(studyId, studyTransactionsRequest);
	}
}
