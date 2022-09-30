package org.ibp.api.rest.dataset.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.TrialServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableTypeGroup;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.study.StudyUpdateRequestDTO;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.dao.dms.InstanceMetadata;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.study.EnvironmentParameter;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.java.impl.middleware.study.validator.StudyUpdateRequestValidator;
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
public class StudyUpdateRequestValidatorTest {

	private static final String STUDY_DBID = "1";
	private static final String TRIAL_DBID = "2";
	private static final String LOCATION_DBID = "3";
	private static final String VARIABLE_DBID = "4";

	@Mock
	private TrialServiceBrapi trialServiceBrapi;

	@Mock
	private LocationService locationService;

	@Mock
	private VariableServiceBrapi variableServiceBrapi;

	@InjectMocks
	private StudyUpdateRequestValidator validator;

	@Before
	public void setUp() {
		final StudySearchFilter filter = new StudySearchFilter();
		filter.setTrialDbIds(Collections.singletonList(TRIAL_DBID));
		final StudySummary studySummary = new StudySummary();
		studySummary.setTrialDbId(Integer.valueOf(TRIAL_DBID));
		final InstanceMetadata instanceMetadata = new InstanceMetadata();
		instanceMetadata.setInstanceDbId(Integer.valueOf(STUDY_DBID));
		studySummary.setInstanceMetaData(Arrays.asList(instanceMetadata));
		Mockito.when(this.trialServiceBrapi.getStudies(ArgumentMatchers.eq(filter), ArgumentMatchers.eq(null)))
			.thenReturn(Collections.singletonList(studySummary));
		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		locationSearchRequest.setLocationIds(Collections.singletonList(Integer.valueOf(LOCATION_DBID)));
		Mockito.when(this.locationService.searchLocations(locationSearchRequest, null, null))
			.thenReturn(Collections.singletonList(new LocationDTO()));

		final VariableDTO variableDTO = new VariableDTO();
		variableDTO.setObservationVariableDbId(VARIABLE_DBID);
		variableDTO.getScale().setDataType(DataType.NUMERIC_VARIABLE.getBrapiName());
		variableDTO.getContextOfUse().add(VariableDTO.ContextOfUseEnum.PLOT.name());
		final VariableSearchRequestDTO variableSearchRequestDTOUsingVariableId = new VariableSearchRequestDTO();
		variableSearchRequestDTOUsingVariableId.setObservationVariableDbIds(Collections.singletonList(VARIABLE_DBID));
		Mockito.when(this.variableServiceBrapi.getVariables(variableSearchRequestDTOUsingVariableId, null, VariableTypeGroup.TRAIT))
			.thenReturn(Collections.singletonList(variableDTO));
	}

	@Test
	public void testValidate_Success() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		final BindingResult result = this.validator.validate(Integer.valueOf(STUDY_DBID), studyUpdateRequestDTO);
		Assert.assertFalse(result.hasErrors());
	}

	@Test
	public void testValidate_WhereTrialDbIdIsNull() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		studyUpdateRequestDTO.setTrialDbId(null);
		final BindingResult result = this.validator.validate(Integer.valueOf(STUDY_DBID), studyUpdateRequestDTO);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.update.trialDbId.required", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testValidate_WhereTrialDbIdIsInvalid() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		studyUpdateRequestDTO.setTrialDbId("99");
		final BindingResult result = this.validator.validate(Integer.valueOf(STUDY_DBID), studyUpdateRequestDTO);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.update.trialDbId.invalid", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testValidate_WhereStudyDbIdIsInvalid() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		studyUpdateRequestDTO.setTrialDbId(TRIAL_DBID);
		final BindingResult result = this.validator.validate(100, studyUpdateRequestDTO);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.update.studyDbId.invalid", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testValidate_WhereLocationDbIdIsInvalid() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		studyUpdateRequestDTO.setLocationDbId("99");
		final BindingResult result = this.validator.validate(Integer.valueOf(STUDY_DBID), studyUpdateRequestDTO);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.update.locationDbId.invalid", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testValidate_WhereObservationVariabldDbIdIsInvalid() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		studyUpdateRequestDTO.setObservationVariableDbIds(Arrays.asList("99"));
		final BindingResult result = this.validator.validate(Integer.valueOf(STUDY_DBID), studyUpdateRequestDTO);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.update.observationVariableDbId.invalid", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testValidate_WhereExternalReferenceHasMissingInfo() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		externalReferenceDTOS.add(new ExternalReferenceDTO());
		studyUpdateRequestDTO.setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.validator.validate(Integer.valueOf(STUDY_DBID), studyUpdateRequestDTO);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.update.reference.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testValidate_WhereExternalReferenceIdExceedsLength() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(2001));
		externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(200));
		externalReferenceDTOS.add(externalReferenceDTO);
		studyUpdateRequestDTO.setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.validator.validate(Integer.valueOf(STUDY_DBID), studyUpdateRequestDTO);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.update.reference.id.exceeded.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testValidate_WhereExternalReferenceSourceExceedsLength() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(200));
		externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(256));
		externalReferenceDTOS.add(externalReferenceDTO);
		studyUpdateRequestDTO.setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.validator.validate(Integer.valueOf(STUDY_DBID), studyUpdateRequestDTO);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.update.reference.source.exceeded.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testValidate_WhereEnvironmentParametersHasNullPUI() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		final List<EnvironmentParameter> environmentParameters = new ArrayList<>();
		final EnvironmentParameter environmentParameter = new EnvironmentParameter();
		environmentParameters.add(environmentParameter);
		studyUpdateRequestDTO.setEnvironmentParameters(environmentParameters);
		final BindingResult result = this.validator.validate(Integer.valueOf(STUDY_DBID), studyUpdateRequestDTO);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.update.environment.parameter.pui.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testValidate_WhereEnvironmentParameterValueExceedsMaxLength() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		final List<EnvironmentParameter> environmentParameters = new ArrayList<>();
		final EnvironmentParameter environmentParameter = new EnvironmentParameter();
		environmentParameter.setParameterPUI(RandomStringUtils.randomNumeric(4));
		environmentParameter.setValue(RandomStringUtils.randomAlphabetic(256));
		environmentParameters.add(environmentParameter);
		studyUpdateRequestDTO.setEnvironmentParameters(environmentParameters);
		final BindingResult result = this.validator.validate(Integer.valueOf(STUDY_DBID), studyUpdateRequestDTO);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.update.environment.parameter.value.exceeded.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testValidate_WhereSeasonsValuesAreMoreThanOne() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = this.createStudyUpdateRequestDTO();
		studyUpdateRequestDTO.setSeasons(Arrays.asList("DRY", "WET"));
		final BindingResult result = this.validator.validate(Integer.valueOf(STUDY_DBID), studyUpdateRequestDTO);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("study.update.season.invalid", result.getAllErrors().get(0).getCode());
	}

	private StudyUpdateRequestDTO createStudyUpdateRequestDTO() {
		final StudyUpdateRequestDTO studyUpdateRequestDTO = new StudyUpdateRequestDTO();
		studyUpdateRequestDTO.setTrialDbId(TRIAL_DBID);
		studyUpdateRequestDTO.setLocationDbId(LOCATION_DBID);
		studyUpdateRequestDTO.setObservationVariableDbIds(Arrays.asList(VARIABLE_DBID));
		return studyUpdateRequestDTO;
	}

}
