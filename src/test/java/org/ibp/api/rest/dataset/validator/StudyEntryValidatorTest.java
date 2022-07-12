package org.ibp.api.rest.dataset.validator;

import com.google.common.collect.Sets;
import org.apache.commons.lang.math.RandomUtils;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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
		Mockito.doReturn(Collections.emptyList()).when(this.middlewareStudyEntryService).getStudyEntries(eq(studyId),
			any(), any());
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
		Mockito.doReturn(Collections.singletonList(new StudyEntryDto(entryId, entryId, "ABC"))).when(this.middlewareStudyEntryService)
			.getStudyEntries(
				eq(studyId),
				any(), any());
		Mockito.doReturn(true).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
		this.validator.validate(studyId, entryId, newGid);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidate_StudyEntryHasPendingInventory() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int entryId = random.nextInt();
		final int newGid = random.nextInt();
		Mockito.doReturn(Collections.singletonList(new StudyEntryDto(entryId, entryId, "ABC"))).when(this.middlewareStudyEntryService)
			.getStudyEntries(
				eq(studyId),
				any(), any());
		Mockito.doReturn(false).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
		Mockito.doReturn(Collections.singletonList(new Transaction())).when(this.plantingService)
			.getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.PENDING);
		this.validator.validate(studyId, entryId, newGid);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidate_StudyEntryHasConfirmedInventory() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int entryId = random.nextInt();
		final int newGid = random.nextInt();
		Mockito.doReturn(Collections.singletonList(new StudyEntryDto(entryId, entryId, "ABC"))).when(this.middlewareStudyEntryService)
			.getStudyEntries(
				eq(studyId),
				any(), any());
		Mockito.doReturn(false).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
		Mockito.doReturn(Collections.emptyList()).when(this.plantingService)
			.getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.PENDING);
		Mockito.doReturn(Collections.singletonList(new Transaction())).when(this.plantingService)
			.getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.CONFIRMED);
		this.validator.validate(studyId, entryId, newGid);
	}

	@Test
	public void testValidate_Successful() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int entryId = random.nextInt();
		final int newGid = random.nextInt();
		Mockito.doReturn(Collections.singletonList(new StudyEntryDto(entryId, entryId, "ABC"))).when(this.middlewareStudyEntryService)
			.getStudyEntries(
				eq(studyId),
				any(), any());
		Mockito.doReturn(false).when(this.sampleService).studyEntryHasSamples(studyId, entryId);
		Mockito.doReturn(Collections.emptyList()).when(this.plantingService)
			.getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.PENDING);
		Mockito.doReturn(Collections.emptyList()).when(this.plantingService)
			.getPlantingTransactionsByStudyAndEntryId(studyId, entryId, TransactionStatus.CONFIRMED);
		this.validator.validate(studyId, entryId, newGid);
		Mockito.verify(this.germplasmValidator).validateGermplasmId(any(), eq(newGid));
	}

	@Test
	public void testValidateStudyContainsEntries_ThrowsRightException() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final List<Integer> entryIds = Collections.singletonList(ran.nextInt());

		try {
			this.validator.validateStudyContainsEntries(studyId, entryIds);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(
				Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("invalid.entryids"));
		}
	}

	@Test
	public void testValidateStudyContainsEntries_SomeEntryNumbersDoNotExist() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Set<String> entryNumbers = Sets.newHashSet("1", "2", "3");
		final List<StudyEntryDto> existingStudyEntries = new ArrayList<>();
		existingStudyEntries.add(new StudyEntryDto(RandomUtils.nextInt(), 1, RandomUtils.nextInt(), ""));
		Mockito.when(this.middlewareStudyEntryService.getStudyEntries(eq(studyId), any(), any())).thenReturn(existingStudyEntries);

		try {
			this.validator.validateStudyContainsEntryNumbers(studyId, entryNumbers);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(
				Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("invalid.entry.numbers"));
			Assert.assertEquals(new Object[] {"2, 3"}, e.getErrors().get(0).getArguments());
		}
	}
}
