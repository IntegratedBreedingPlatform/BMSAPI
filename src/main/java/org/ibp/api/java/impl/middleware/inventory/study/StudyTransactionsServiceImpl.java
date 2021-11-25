package org.ibp.api.java.impl.middleware.inventory.study;

import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.inventory.manager.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class StudyTransactionsServiceImpl implements StudyTransactionsService {

	@Autowired
	private org.generationcp.middleware.api.inventory.study.StudyTransactionsService studyTransactionsService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private StudyValidator studyValidator;

	@Override
	public long countStudyTransactions(final Integer studyId, final StudyTransactionsRequest studyTransactionsRequest) {
		this.studyValidator.validate(studyId, false);
		return studyTransactionsService.countStudyTransactions(studyId, studyTransactionsRequest);
	}

	@Override
	public List<StudyTransactionsDto> searchStudyTransactions(final Integer studyId,
		final StudyTransactionsRequest studyTransactionsRequest, final Pageable pageable) {

		this.studyValidator.validate(studyId, false);
		return this.studyTransactionsService.searchStudyTransactions(studyId, studyTransactionsRequest, pageable);
	}

	@Override
	public void cancelPendingTransactions(final Integer studyId, final SearchCompositeDto<Integer, Integer> searchCompositeDto) {
		this.studyValidator.validate(studyId, true);
		this.transactionService.cancelPendingTransactions(searchCompositeDto);
	}

	@Override
	public StudyTransactionsDto getStudyTransactionByTransactionId(final Integer transactionId) {
		BaseValidator.checkNotNull(transactionId, "transaction.id.null");

		final MapBindingResult errors =
				new MapBindingResult(new HashMap<String, String>(), GermplasmListValidator.class.getName());

		final TransactionsSearchDto transactionsSearchDto = new TransactionsSearchDto();
		transactionsSearchDto.setTransactionIds(Arrays.asList(transactionId));
		final List<TransactionDto> transactions = this.transactionService.searchTransactions(transactionsSearchDto, null);
		if (CollectionUtils.isEmpty(transactions) || !transactions.get(0).getTransactionId().equals(transactionId)) {
			errors.reject("transactions.not.found", new String[] {String.valueOf(transactionId)}, "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		return this.studyTransactionsService.getStudyTransactionByTransactionId(transactionId);
	}

}
