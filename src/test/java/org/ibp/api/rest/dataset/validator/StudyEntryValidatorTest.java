package org.ibp.api.rest.dataset.validator;

import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.service.api.SampleService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.impl.inventory.PlantingServiceImpl;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StudyEntryValidatorTest {

    @Mock
    private GermplasmValidator germplasmValidator;

    @Mock
    private PlantingServiceImpl plantingService;

    @Mock
    private SampleService sampleService;

    @Mock
    private StudyEntryService middlewareStudyEntryService;

    @Mock
    private StudyValidator studyValidator;

    @InjectMocks
    private final StudyEntryValidator validator = new StudyEntryValidator();

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
        Mockito.doReturn(Collections.emptyList()).when(this.middlewareStudyEntryService).getStudyEntries(ArgumentMatchers.eq(studyId),
                ArgumentMatchers.any(), ArgumentMatchers.any());
        this.validator.validate(studyId, entryId, newGid);
    }

    @Test(expected = ApiRequestValidationException.class)
    public void testValidateStudyAlreadyHasStudyEntries() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        Mockito.doReturn(new Long(5)).when(this.middlewareStudyEntryService).countStudyEntries(studyId);
        this.validator.validateStudyAlreadyHasStudyEntries(studyId);
    }

    @Test(expected = ApiRequestValidationException.class)
    public void testValidate_StudyEntryHasSamples() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        Mockito.doReturn(Collections.singletonList(new StudyEntryDto(entryId, entryId, "ABC"))).when(this.middlewareStudyEntryService).getStudyEntries(ArgumentMatchers.eq(studyId),
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
        Mockito.doReturn(Collections.singletonList(new StudyEntryDto(entryId, entryId, "ABC"))).when(this.middlewareStudyEntryService).getStudyEntries(ArgumentMatchers.eq(studyId),
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
        Mockito.doReturn(Collections.singletonList(new StudyEntryDto(entryId, entryId, "ABC"))).when(this.middlewareStudyEntryService).getStudyEntries(ArgumentMatchers.eq(studyId),
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
        Mockito.doReturn(Collections.singletonList(new StudyEntryDto(entryId, entryId, "ABC"))).when(this.middlewareStudyEntryService).getStudyEntries(ArgumentMatchers.eq(studyId),
                ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.doReturn(false).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
        Mockito.doReturn(Collections.emptyList()).when(this.plantingService).getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.PENDING);
        Mockito.doReturn(Collections.emptyList()).when(this.plantingService).getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.CONFIRMED);
        this.validator.validate(studyId, entryId, newGid);
        Mockito.verify(this.germplasmValidator).validateGermplasmId(ArgumentMatchers.any(), ArgumentMatchers.eq(newGid));
    }

    @Test(expected = ApiRequestValidationException.class)
    public void testValidateStudyEntriesForUpdate() {
        final List<Integer> entryIds = Collections.singletonList(1);
        Mockito.doReturn(entryIds).when(this.middlewareStudyEntryService).hasPlotEntries(entryIds);
        this.validator.validateStudyEntriesForUpdate(entryIds);
    }
}
