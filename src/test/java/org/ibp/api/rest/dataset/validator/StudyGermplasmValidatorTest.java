package org.ibp.api.rest.dataset.validator;

import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.service.api.SampleService;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.impl.inventory.PlantingServiceImpl;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyGermplasmValidator;
import org.ibp.api.java.study.StudyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

public class StudyGermplasmValidatorTest {

    @Mock
    private GermplasmValidator germplasmValidator;

    @Mock
    private PlantingServiceImpl plantingService;

    @Mock
    private StudyService studyService;

    @Mock
    private SampleService sampleService;

    @Mock
    private org.generationcp.middleware.service.api.study.StudyGermplasmService middlewareStudyGermplasmService;

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
        Mockito.doReturn(Optional.empty()).when(this.middlewareStudyGermplasmService).getStudyGermplasm(studyId, entryId);
        this.validator.validate(studyId, entryId, newGid);
    }

    @Test(expected = ApiRequestValidationException.class)
    public void testValidate_StudyHasMeansDataset() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        Mockito.doReturn(Optional.of(new StudyGermplasmDto(entryId))).when(this.middlewareStudyGermplasmService).getStudyGermplasm(studyId, entryId);
        Mockito.doReturn(true).when(this.studyService).studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId());
        this.validator.validate(studyId, entryId, newGid);
    }

    @Test(expected = ApiRequestValidationException.class)
    public void testValidate_StudyHasAdvanceOrCrossList() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        Mockito.doReturn(Optional.of(new StudyGermplasmDto(entryId))).when(this.middlewareStudyGermplasmService).getStudyGermplasm(studyId, entryId);
        Mockito.doReturn(false).when(this.studyService).studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId());
        Mockito.doReturn(true).when(this.studyService).hasAdvancedOrCrossesList(studyId);
        this.validator.validate(studyId, entryId, newGid);
    }

    @Test(expected = ApiRequestValidationException.class)
    public void testValidate_StudyEntryHasSamples() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        Mockito.doReturn(Optional.of(new StudyGermplasmDto(entryId))).when(this.middlewareStudyGermplasmService).getStudyGermplasm(studyId, entryId);
        Mockito.doReturn(false).when(this.studyService).studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId());
        Mockito.doReturn(false).when(this.studyService).hasAdvancedOrCrossesList(studyId);
        Mockito.doReturn(true).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
        this.validator.validate(studyId, entryId, newGid);
    }

    @Test(expected = ApiRequestValidationException.class)
    public void testValidate_StudyEntryHasPendingInventory() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        Mockito.doReturn(Optional.of(new StudyGermplasmDto(entryId))).when(this.middlewareStudyGermplasmService).getStudyGermplasm(studyId, entryId);
        Mockito.doReturn(false).when(this.studyService).studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId());
        Mockito.doReturn(false).when(this.studyService).hasAdvancedOrCrossesList(studyId);
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
        Mockito.doReturn(Optional.of(new StudyGermplasmDto(entryId))).when(this.middlewareStudyGermplasmService).getStudyGermplasm(studyId, entryId);
        Mockito.doReturn(false).when(this.studyService).studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId());
        Mockito.doReturn(false).when(this.studyService).hasAdvancedOrCrossesList(studyId);
        Mockito.doReturn(false).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
        Mockito.doReturn(Collections.emptyList()).when(this.plantingService).getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.PENDING);
        Mockito.doReturn(Collections.singletonList(new Transaction())).when(this.plantingService).getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.CONFIRMED);
        this.validator.validate(studyId, entryId, newGid);
    }

    public void testValidate_Succesful() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int entryId = random.nextInt();
        final int newGid = random.nextInt();
        Mockito.doReturn(Optional.of(new StudyGermplasmDto(entryId))).when(this.middlewareStudyGermplasmService).getStudyGermplasm(studyId, entryId);
        Mockito.doReturn(false).when(this.studyService).studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId());
        Mockito.doReturn(false).when(this.studyService).hasAdvancedOrCrossesList(studyId);
        Mockito.doReturn(false).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
        Mockito.doReturn(Collections.emptyList()).when(this.plantingService).getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.PENDING);
        Mockito.doReturn(Collections.emptyList()).when(this.plantingService).getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.CONFIRMED);
        this.validator.validate(studyId, entryId, newGid);
        Mockito.verify(this.germplasmValidator).validateGermplasmId(ArgumentMatchers.any(), ArgumentMatchers.eq(newGid));
    }
}
