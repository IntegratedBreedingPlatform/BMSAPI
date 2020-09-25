package org.ibp.api.rest.dataset.validator;

import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.service.api.SampleService;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.impl.inventory.PlantingServiceImpl;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyGermplasmValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

public class StudyGermplasmValidatorTest {

    @Mock
    private GermplasmValidator germplasmValidator;

    @Mock
    private PlantingServiceImpl plantingService;

    @Mock
    private SampleService sampleService;

    @Mock
    private org.generationcp.middleware.service.api.study.StudyGermplasmService middlewareStudyGermplasmService;

    @Mock
    private StudyValidator studyValidator;

    @InjectMocks
    private StudyGermplasmValidator validator = new StudyGermplasmValidator();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = ApiRequestValidationException.class)
    public void testValidate_InvalidEntryId() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        Mockito.doReturn(Optional.empty()).when(this.middlewareStudyGermplasmService).getStudyEntries(ArgumentMatchers.eq(studyId),
                ArgumentMatchers.any(), ArgumentMatchers.any());
        this.validator.validate(studyId, entryId, newGid);
    }

    @Test(expected = ApiRequestValidationException.class)
    public void testValidate_StudyEntryHasSamples() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        Mockito.doReturn(Optional.of(new StudyGermplasmDto(entryId))).when(this.middlewareStudyGermplasmService).getStudyEntries(ArgumentMatchers.eq(studyId),
                ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.doReturn(true).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
        this.validator.validate(studyId, entryId, newGid);
    }

    @Test(expected = ApiRequestValidationException.class)
    public void testValidate_StudyEntryHasPendingInventory() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        Mockito.doReturn(Optional.of(new StudyGermplasmDto(entryId))).when(this.middlewareStudyGermplasmService).getStudyEntries(ArgumentMatchers.eq(studyId),
                ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.doReturn(false).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
        Mockito.doReturn(Collections.singletonList(new Transaction())).when(this.plantingService).getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.PENDING);
        this.validator.validate(studyId, entryId, newGid);
    }

    @Test(expected = ApiRequestValidationException.class)
    public void testValidate_StudyEntryHasConfirmedInventory() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        Mockito.doReturn(Optional.of(new StudyGermplasmDto(entryId))).when(this.middlewareStudyGermplasmService).getStudyEntries(ArgumentMatchers.eq(studyId),
                ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.doReturn(false).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
        Mockito.doReturn(Collections.emptyList()).when(this.plantingService).getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.PENDING);
        Mockito.doReturn(Collections.singletonList(new Transaction())).when(this.plantingService).getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.CONFIRMED);
        this.validator.validate(studyId, entryId, newGid);
    }

    @Test
    public void testValidate_Successful() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        Mockito.doReturn(Optional.of(new StudyGermplasmDto(entryId))).when(this.middlewareStudyGermplasmService).getStudyEntries(ArgumentMatchers.eq(studyId),
                ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.doReturn(false).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
        Mockito.doReturn(Collections.emptyList()).when(this.plantingService).getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.PENDING);
        Mockito.doReturn(Collections.emptyList()).when(this.plantingService).getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.CONFIRMED);
        this.validator.validate(studyId, entryId, newGid);
        Mockito.verify(this.germplasmValidator).validateGermplasmId(ArgumentMatchers.any(), ArgumentMatchers.eq(newGid));
    }
}
