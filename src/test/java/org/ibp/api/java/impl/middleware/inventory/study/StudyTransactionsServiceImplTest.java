package org.ibp.api.java.impl.middleware.inventory.study;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.inventory.manager.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class StudyTransactionsServiceImplTest {

  private static final Integer TRANSACTION_ID = new Random().nextInt();

  @InjectMocks
  private StudyTransactionsServiceImpl studyTransactionsService;

  @Mock
  private TransactionService transactionService;

  @Mock
  private org.generationcp.middleware.api.inventory.study.StudyTransactionsService studyTransactionsServiceMiddleware;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testGetStudyTransactionByTransactionId_OK() {
    final TransactionDto transactionDto = Mockito.mock(TransactionDto.class);
    Mockito.when(transactionDto.getTransactionId()).thenReturn(TRANSACTION_ID);

    final StudyTransactionsDto studyTransactionsDto = Mockito.mock(StudyTransactionsDto.class);

    Mockito.when(this.transactionService
        .searchTransactions(ArgumentMatchers.any(TransactionsSearchDto.class), ArgumentMatchers.isNull()))
        .thenReturn(Arrays.asList(transactionDto));

    Mockito.when(this.studyTransactionsServiceMiddleware.getStudyTransactionByTransactionId(TRANSACTION_ID))
        .thenReturn(studyTransactionsDto);

    final StudyTransactionsDto actualStudyTransaction =
        this.studyTransactionsService.getStudyTransactionByTransactionId(TRANSACTION_ID);
    assertThat(actualStudyTransaction, is(studyTransactionsDto));

    Mockito.verify(this.transactionService)
        .searchTransactions(ArgumentMatchers.any(TransactionsSearchDto.class), ArgumentMatchers.isNull());
    Mockito.verifyNoMoreInteractions(this.transactionService);

    Mockito.verify(this.studyTransactionsServiceMiddleware).getStudyTransactionByTransactionId(TRANSACTION_ID);
    Mockito.verifyNoMoreInteractions(this.studyTransactionsServiceMiddleware);
  }

  @Test
  public void testGetStudyTransactionByTransactionId_InvalidNullTransactionId() {
    try {
      this.studyTransactionsService.getStudyTransactionByTransactionId(null);
      fail("Should have failed");
    } catch (final Exception e) {
      assertThat(e, instanceOf(ApiRequestValidationException.class));
      assertThat(Arrays.asList(((ApiRequestValidationException) e).getErrors().get(0).getCodes()),
          hasItem("transaction.id.null"));
    }

    Mockito.verifyNoInteractions(this.transactionService);
    Mockito.verifyNoInteractions(this.studyTransactionsServiceMiddleware);
  }

  @Test
  public void testGetStudyTransactionByTransactionId_TransactionNotFound() {

    Mockito.when(this.transactionService
        .searchTransactions(ArgumentMatchers.any(TransactionsSearchDto.class), ArgumentMatchers.isNull()))
        .thenReturn(new ArrayList<>());

    try {
      this.studyTransactionsService.getStudyTransactionByTransactionId(TRANSACTION_ID);
      fail("Should have failed");
    } catch (final Exception e) {
      assertThat(e, instanceOf(ResourceNotFoundException.class));
      final ResourceNotFoundException exception = (ResourceNotFoundException) e;
      assertThat(Arrays.asList((exception).getError().getCodes()),
          hasItem("transactions.not.found"));
      assertThat(exception.getError().getArguments().length, is(1));
      assertThat(exception.getError().getArguments()[0], is(String.valueOf(TRANSACTION_ID)));
    }

    Mockito.verify(this.transactionService)
        .searchTransactions(ArgumentMatchers.any(TransactionsSearchDto.class), ArgumentMatchers.isNull());
    Mockito.verifyNoMoreInteractions(this.transactionService);

    Mockito.verifyNoInteractions(this.studyTransactionsServiceMiddleware);
  }

}
