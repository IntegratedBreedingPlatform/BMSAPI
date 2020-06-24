package org.ibp.api.java.impl.middleware.inventory.study;

import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudyTransactionsServiceImpl implements StudyTransactionsService {

	@Autowired
	private org.generationcp.middleware.api.inventory.study.StudyTransactionsService studyTransactionsService;

	@Autowired
	private StudyValidator studyValidator;

	@Override
	public long countStudyTransactions(final Integer studyId, final StudyTransactionsRequest studyTransactionsRequest) {
		this.studyValidator.validate(studyId, false);
		return studyTransactionsService.countStudyTransactions(studyId, studyTransactionsRequest);
	}

	@Override
	public List<StudyTransactionsDto> searchStudyTransactions(final Integer studyId,
		final StudyTransactionsRequest studyTransactionsRequest) {

		this.studyValidator.validate(studyId, false);
		return this.studyTransactionsService.searchStudyTransactions(studyId, studyTransactionsRequest);
	}
}
