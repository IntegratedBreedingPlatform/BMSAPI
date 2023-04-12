package org.ibp.api.rest.dataset.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.TrialServiceBrapi;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.dms.TrialSummary;
import org.generationcp.middleware.domain.search_request.brapi.v2.TrialSearchRequestDTO;
import org.generationcp.middleware.service.api.study.EnvironmentParameter;
import org.ibp.api.java.impl.middleware.study.validator.StudyImportRequestValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class StudyImportRequestValidatorTest {

	private static final String TRIAL_DBID = "1";
	private static final String LOCATION_DBID = "1";

	@Mock
	private TrialServiceBrapi trialServiceBrapi;

	@Mock
	private LocationService locationService;

	@InjectMocks
	private StudyImportRequestValidator validator;

	@Before
	public void setUp() {
		final TrialSearchRequestDTO trialSearchRequestDTO = new TrialSearchRequestDTO();
		trialSearchRequestDTO.setTrialDbIds(Collections.singletonList(TRIAL_DBID));
		final TrialSummary trialSummary = new TrialSummary();
		trialSummary.setTrialDbId(Integer.valueOf(TRIAL_DBID));
		Mockito.when(this.trialServiceBrapi.searchTrials(ArgumentMatchers.eq(trialSearchRequestDTO), ArgumentMatchers.eq(null)))
			.thenReturn(Collections.singletonList(trialSummary));
		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		locationSearchRequest.setLocationDbIds(Collections.singletonList(Integer.valueOf(LOCATION_DBID)));
		Mockito.when(this.locationService.searchLocations(locationSearchRequest, null, null))
			.thenReturn(Collections.singletonList(new LocationDTO()));
	}

	@Test
	public void testPruneStudiesInvalidForImport_Success() {
		final List<StudyImportRequestDTO> studyImportRequestDTOS = this.createStudyImportRequestDTOList();
		final BindingResult result = this.validator.pruneStudiesInvalidForImport(studyImportRequestDTOS);
		Assert.assertFalse(result.hasErrors());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereTrialDbIdIsNull() {
		final List<StudyImportRequestDTO> studyImportRequestDTOS = this.createStudyImportRequestDTOList();
		studyImportRequestDTOS.get(0).setTrialDbId(null);
		final BindingResult result = this.validator.pruneStudiesInvalidForImport(studyImportRequestDTOS);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.import.trialDbId.required", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereTrialDbIdIsInvalid() {
		final List<StudyImportRequestDTO> studyImportRequestDTOS = this.createStudyImportRequestDTOList();
		studyImportRequestDTOS.get(0).setTrialDbId("2");
		final BindingResult result = this.validator.pruneStudiesInvalidForImport(studyImportRequestDTOS);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.import.trialDbId.invalid", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereLocationDbIdIsInvalid() {
		final List<StudyImportRequestDTO> studyImportRequestDTOS = this.createStudyImportRequestDTOList();
		studyImportRequestDTOS.get(0).setLocationDbId("2");
		final BindingResult result = this.validator.pruneStudiesInvalidForImport(studyImportRequestDTOS);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.import.locationDbId.invalid", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereExternalReferenceHasMissingInfo() {
		final List<StudyImportRequestDTO> studyImportRequestDTOS = this.createStudyImportRequestDTOList();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		externalReferenceDTOS.add(new ExternalReferenceDTO());
		studyImportRequestDTOS.get(0).setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.validator.pruneStudiesInvalidForImport(studyImportRequestDTOS);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.import.reference.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereExternalReferenceIdExceedsLength() {
		final List<StudyImportRequestDTO> studyImportRequestDTOS = this.createStudyImportRequestDTOList();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(2001));
		externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(200));
		externalReferenceDTOS.add(externalReferenceDTO);
		studyImportRequestDTOS.get(0).setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.validator.pruneStudiesInvalidForImport(studyImportRequestDTOS);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.import.reference.id.exceeded.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereExternalReferenceSourceExceedsLength() {
		final List<StudyImportRequestDTO> studyImportRequestDTOS = this.createStudyImportRequestDTOList();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(200));
		externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(256));
		externalReferenceDTOS.add(externalReferenceDTO);
		studyImportRequestDTOS.get(0).setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.validator.pruneStudiesInvalidForImport(studyImportRequestDTOS);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.import.reference.source.exceeded.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereEnvironmentParametersHasNullPUI() {
		final List<StudyImportRequestDTO> studyImportRequestDTOS = this.createStudyImportRequestDTOList();
		final List<EnvironmentParameter> environmentParameters = new ArrayList<>();
		final EnvironmentParameter environmentParameter = new EnvironmentParameter();
		environmentParameters.add(environmentParameter);
		studyImportRequestDTOS.get(0).setEnvironmentParameters(environmentParameters);
		final BindingResult result = this.validator.pruneStudiesInvalidForImport(studyImportRequestDTOS);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.import.environment.parameter.pui.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneTrialsInvalidForImport_WhereEnvironmentParameterValueExceedsMaxLength() {
		final List<StudyImportRequestDTO> studyImportRequestDTOS = this.createStudyImportRequestDTOList();
		final List<EnvironmentParameter> environmentParameters = new ArrayList<>();
		final EnvironmentParameter environmentParameter = new EnvironmentParameter();
		environmentParameter.setParameterPUI(RandomStringUtils.randomNumeric(4));
		environmentParameter.setValue(RandomStringUtils.randomAlphabetic(256));
		environmentParameters.add(environmentParameter);
		studyImportRequestDTOS.get(0).setEnvironmentParameters(environmentParameters);
		final BindingResult result = this.validator.pruneStudiesInvalidForImport(studyImportRequestDTOS);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.import.environment.parameter.value.exceeded.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereSeasonsValuesAreMoreThanOne() {
		final List<StudyImportRequestDTO> studyImportRequestDTOS = this.createStudyImportRequestDTOList();
		studyImportRequestDTOS.get(0).setSeasons(Arrays.asList("DRY", "WET"));
		final BindingResult result = this.validator.pruneStudiesInvalidForImport(studyImportRequestDTOS);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.import.season.invalid", result.getAllErrors().get(0).getCode());
	}

	private List<StudyImportRequestDTO> createStudyImportRequestDTOList() {
		final List<StudyImportRequestDTO> studyImportRequestDTOS = new ArrayList<>();
		final StudyImportRequestDTO studyImportRequestDTO = new StudyImportRequestDTO();
		studyImportRequestDTO.setTrialDbId(TRIAL_DBID);
		studyImportRequestDTO.setLocationDbId(LOCATION_DBID);
		studyImportRequestDTOS.add(studyImportRequestDTO);
		return studyImportRequestDTOS;
	}

}
