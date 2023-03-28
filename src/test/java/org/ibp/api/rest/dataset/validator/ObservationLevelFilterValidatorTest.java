package org.ibp.api.rest.dataset.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.StudyServiceBrapi;
import org.generationcp.middleware.api.brapi.TrialServiceBrapi;
import org.generationcp.middleware.api.brapi.v2.observationlevel.ObservationLevelFilter;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.domain.search_request.brapi.v2.TrialSearchRequestDTO;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.hamcrest.MatcherAssert;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.observationunits.ObservationLevelFilterValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItem;

@RunWith(MockitoJUnitRunner.class)
public class ObservationLevelFilterValidatorTest {
	private static final String TRIAL_DBID = "1";
	private static final String STUDY_DBID = "1";
	private static final String PROGRAM_DBID = RandomStringUtils.randomAlphabetic(10);
	private static final String CROP = RandomStringUtils.randomAlphabetic(10);

	@Mock
	private ProgramService programService;

	@Mock
	private StudyServiceBrapi studyServiceBrapi;

	@Mock
	private TrialServiceBrapi trialServiceBrapi;

	@InjectMocks
	private ObservationLevelFilterValidator validator;

	@Test
	public void testValidateObservationLevelFilter_Success() {
		final ObservationLevelFilter filter = new ObservationLevelFilter(STUDY_DBID, TRIAL_DBID, PROGRAM_DBID);

		Mockito.when(this.programService.getProjectByUuidAndCrop(PROGRAM_DBID, CROP)).thenReturn(new Project());
		final StudySearchFilter studySearchFilter = new StudySearchFilter();
		studySearchFilter.setStudyDbIds(Collections.singletonList(filter.getStudyDbId()));
		final StudyInstanceDto studyInstanceDto = new StudyInstanceDto();
		studyInstanceDto.setTrialDbId(TRIAL_DBID);
		studyInstanceDto.setStudyDbId(STUDY_DBID);
		studyInstanceDto.setProgramDbId(PROGRAM_DBID);
		Mockito.when(this.studyServiceBrapi.getStudyInstances(studySearchFilter, null))
			.thenReturn(Collections.singletonList(studyInstanceDto));
		try {
			this.validator.validate(filter, CROP);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("No ApiRequestValidationException should be thrown.");
		}
	}

	@Test
	public void testValidateObservationLevelFilter_WithoutStudyDbId_Success() {
		final ObservationLevelFilter filter = new ObservationLevelFilter(null, TRIAL_DBID, PROGRAM_DBID);

		Mockito.when(this.programService.getProjectByUuidAndCrop(PROGRAM_DBID, CROP)).thenReturn(new Project());
		final TrialSearchRequestDTO trialSearchRequestDTO = new TrialSearchRequestDTO();
		trialSearchRequestDTO.setTrialDbIds(Collections.singletonList(filter.getTrialDbId()));
		final StudySummary studySummary = new StudySummary();
		studySummary.setTrialDbId(Integer.valueOf(TRIAL_DBID));
		studySummary.setProgramDbId(PROGRAM_DBID);
		Mockito.when(this.trialServiceBrapi.searchTrials(trialSearchRequestDTO, null))
			.thenReturn(Collections.singletonList(studySummary));
		try {
			this.validator.validate(filter, CROP);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("No ApiRequestValidationException should be thrown.");
		}
	}

	@Test
	public void testValidateObservationLevelFilter_InvalidProgramDbId() {
		final ObservationLevelFilter filter = new ObservationLevelFilter(STUDY_DBID, TRIAL_DBID, PROGRAM_DBID);

		Mockito.when(this.programService.getProjectByUuidAndCrop(PROGRAM_DBID, CROP)).thenReturn(null);
		try {
			this.validator.validate(filter, CROP);
			Assert.fail("ApiRequestValidationException should be thrown.");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert
				.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("observation.level.invalid.programdbid"));
		}
	}

	@Test
	public void testValidateObservationLevelFilter_InvalidStudyDbId() {
		final ObservationLevelFilter filter = new ObservationLevelFilter(STUDY_DBID, TRIAL_DBID, PROGRAM_DBID);

		Mockito.when(this.programService.getProjectByUuidAndCrop(PROGRAM_DBID, CROP)).thenReturn(new Project());
		try {
			this.validator.validate(filter, CROP);
			Assert.fail("ApiRequestValidationException should be thrown.");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert
				.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("studydbid.invalid"));
		}
	}

	@Test
	public void testValidateObservationLevelFilter_InvalidObservationFilters() {
		final ObservationLevelFilter filter = new ObservationLevelFilter(STUDY_DBID, TRIAL_DBID, PROGRAM_DBID);

		Mockito.when(this.programService.getProjectByUuidAndCrop(PROGRAM_DBID, CROP)).thenReturn(new Project());
		final StudySearchFilter studySearchFilter = new StudySearchFilter();
		studySearchFilter.setStudyDbIds(Collections.singletonList(filter.getStudyDbId()));
		final StudyInstanceDto studyInstanceDto = new StudyInstanceDto();
		studyInstanceDto.setTrialDbId(RandomStringUtils.randomNumeric(5));
		studyInstanceDto.setStudyDbId(STUDY_DBID);
		studyInstanceDto.setProgramDbId(PROGRAM_DBID);
		Mockito.when(this.studyServiceBrapi.getStudyInstances(studySearchFilter, null))
			.thenReturn(Collections.singletonList(studyInstanceDto));

		try {
			this.validator.validate(filter, CROP);
			Assert.fail("ApiRequestValidationException should be thrown.");
		} catch (final ApiRequestValidationException e) {
			MatcherAssert
				.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("observation.level.invalid"));
		}
	}

	@Test
	public void testValidateObservationLevelFilter_InvalidTrialDbId() {
		final ObservationLevelFilter filter = new ObservationLevelFilter(null, TRIAL_DBID, PROGRAM_DBID);

		Mockito.when(this.programService.getProjectByUuidAndCrop(PROGRAM_DBID, CROP)).thenReturn(new Project());
		final TrialSearchRequestDTO trialSearchRequestDTO = new TrialSearchRequestDTO();
		trialSearchRequestDTO.setTrialDbIds(Collections.singletonList(filter.getTrialDbId()));
		Mockito.when(this.trialServiceBrapi.searchTrials(trialSearchRequestDTO, null))
			.thenReturn(null);
		try {
			this.validator.validate(filter, CROP);
		} catch (final ApiRequestValidationException e) {
			MatcherAssert
				.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("observation.level.invalid.trialdbid"));
		}
	}

	@Test
	public void testValidateObservationLevelFilter_InvalidObservationFiltersForTrialDbId() {
		final ObservationLevelFilter filter = new ObservationLevelFilter(null, TRIAL_DBID, PROGRAM_DBID);

		Mockito.when(this.programService.getProjectByUuidAndCrop(PROGRAM_DBID, CROP)).thenReturn(new Project());
		final TrialSearchRequestDTO trialSearchRequestDTO = new TrialSearchRequestDTO();
		trialSearchRequestDTO.setTrialDbIds(Collections.singletonList(filter.getTrialDbId()));
		final StudySummary studySummary = new StudySummary();
		studySummary.setTrialDbId(Integer.valueOf(TRIAL_DBID));
		studySummary.setProgramDbId(RandomStringUtils.randomAlphanumeric(15));
		Mockito.when(this.trialServiceBrapi.searchTrials(trialSearchRequestDTO, null))
			.thenReturn(Collections.singletonList(studySummary));
		try {
			this.validator.validate(filter, CROP);
		} catch (final ApiRequestValidationException e) {
			MatcherAssert
				.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("observation.level.invalid"));
		}
	}
}
