package org.ibp.api.rest.dataset.validator;

import com.google.inject.matcher.Matchers;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.GermplasmServiceBrapi;
import org.generationcp.middleware.api.brapi.StudyServiceBrapi;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationLevelRelationship;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportRequestDto;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitPosition;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmSearchRequest;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.ibp.api.java.impl.middleware.observationunits.ObservationUnitImportRequestValidator;
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
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ObservationUnitImportRequestValidatorTest {

	private static final String GERMPLASM_DBID = RandomStringUtils.randomAlphabetic(8);
	private static final String TRIAL_DBID = "1";
	private static final String STUDY_DBID = "1";
	private static final String PROGRAM_DBID = RandomStringUtils.randomAlphabetic(10);

	@Mock
	private StudyServiceBrapi studyServiceBrapi;

	@Mock
	private GermplasmServiceBrapi germplasmService;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private ObservationUnitService observationUnitService;

	@InjectMocks
	private ObservationUnitImportRequestValidator validator;

	@Before
	public void setUp() {
		final StudyInstanceDto studyInstanceDto = new StudyInstanceDto();
		studyInstanceDto.setTrialDbId(TRIAL_DBID);
		studyInstanceDto.setProgramDbId(PROGRAM_DBID);
		studyInstanceDto.setStudyDbId(STUDY_DBID);

		final StudySearchFilter filter = new StudySearchFilter();
		filter.setStudyDbIds(Collections.singletonList(STUDY_DBID));
		Mockito.when(this.studyServiceBrapi.getStudyInstances(filter, null))
			.thenReturn(Collections.singletonList(studyInstanceDto));

		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(GERMPLASM_DBID);
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGermplasmDbIds(Collections.singletonList(GERMPLASM_DBID));
		Mockito.when(this.germplasmService.searchGermplasmDTO(germplasmSearchRequest, null))
			.thenReturn(Collections.singletonList(germplasmDTO));

		final StandardVariable s = new StandardVariable();
		s.setEnumerations(new ArrayList<>());
		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), null)).thenReturn(s);

		final StandardVariable standardVariable = new StandardVariable();
		final Enumeration enumeration = new Enumeration();
		enumeration.setDescription(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName());
		standardVariable.setEnumerations(Collections.singletonList(enumeration));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), PROGRAM_DBID)).thenReturn(standardVariable);
	}

	@Test
	public void testPruneObservationUnitsInvalidForImport_Success() {
		final StudyInstanceDto studyInstanceDto = new StudyInstanceDto();
		studyInstanceDto.setTrialDbId(TRIAL_DBID);
		studyInstanceDto.setProgramDbId(PROGRAM_DBID);
		studyInstanceDto.setStudyDbId(STUDY_DBID);
		Mockito.when(this.studyServiceBrapi.getStudyInstances(ArgumentMatchers.any(), ArgumentMatchers.isNull()))
			.thenReturn(Collections.singletonList(studyInstanceDto));

		Map<String, List<String>> plotObservationLevelRelationshipsByGeolocations = new HashMap();
		plotObservationLevelRelationshipsByGeolocations.put(STUDY_DBID, Arrays.asList("1"));
		Mockito.when(this.observationUnitService.getPlotObservationLevelRelationshipsByGeolocations(new HashSet<>(Arrays.asList(STUDY_DBID))))
			.thenReturn(plotObservationLevelRelationshipsByGeolocations);

		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(GERMPLASM_DBID);
		Mockito.when(this.germplasmService.searchGermplasmDTO(ArgumentMatchers.any(), ArgumentMatchers.isNull()))
			.thenReturn(Collections.singletonList(germplasmDTO));

		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = new ArrayList();
		observationUnitImportRequestDtos.add(this.createObservationUnitImportRequestDto("2"));
		observationUnitImportRequestDtos.add(this.createObservationUnitImportRequestDto("3"));

		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertFalse(result.hasErrors());
	}

	@Test
	public void testPruneObservationUnitsInvalidForImport_WhereStudyDbIdIsNull() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).setStudyDbId(null);
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.studyDbId.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneObservationUnitsInvalidForImport_WhereProgramDbIdIsNull() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).setProgramDbId(null);
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.programDbId.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneObservationUnitsInvalidForImport_WhereTrialDbIdIsNull() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).setTrialDbId(null);
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.trialDbId.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneObservationUnitsInvalidForImport_WhereStudyDbIdIsInvalid() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).setStudyDbId("2");
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.no.study", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneObservationUnitsInvalidForImport_WhereProgramDbIdIsInvalid() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).setProgramDbId("2");
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.no.study", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneObservationUnitsInvalidForImport_WhereTrialDbIdIsInvalid() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).setTrialDbId("2");
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.no.study", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereEntryTypeIsNull() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).setObservationUnitPosition(null);
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.entry.type.required", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereEntryTypeIsInvalid() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).getObservationUnitPosition().setEntryType("T");
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.entry.type.invalid", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WherePositionCoordinateXIsNull() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).getObservationUnitPosition().setPositionCoordinateX(null);
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.position.invalid", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WherePositionCoordinateYIsNull() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).getObservationUnitPosition().setPositionCoordinateY(null);
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.position.invalid", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereExternalReferenceHasMissingInfo() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		externalReferenceDTOS.add(new ExternalReferenceDTO());
		observationUnitImportRequestDtos.get(0).setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.reference.null", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereExternalReferenceIdExceedsLength() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(2001));
		externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(200));
		externalReferenceDTOS.add(externalReferenceDTO);
		observationUnitImportRequestDtos.get(0).setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.reference.id.exceeded.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereExternalReferenceSourceExceedsLength() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(200));
		externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(256));
		externalReferenceDTOS.add(externalReferenceDTO);
		observationUnitImportRequestDtos.get(0).setExternalReferences(externalReferenceDTOS);
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.reference.source.exceeded.length", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WhereInvalidLevelName() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).getObservationUnitPosition().getObservationLevelRelationships().get(0).setLevelName("REP_NO");
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.invalid.observation.level.name", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneStudiesInvalidForImport_WherePlotNoIsNotIncluded() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).getObservationUnitPosition().setObservationLevelRelationships(null);
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.no.plot", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneObservationUnitsInvalidForImport_importExistingPlotCode() {
		Map<String, List<String>> plotObservationLevelRelationshipsByGeolocations = new HashMap();
		plotObservationLevelRelationshipsByGeolocations.put(STUDY_DBID, Arrays.asList("1"));
		Mockito.when(this.observationUnitService.getPlotObservationLevelRelationshipsByGeolocations(new HashSet<>(Arrays.asList(STUDY_DBID))))
			.thenReturn(plotObservationLevelRelationshipsByGeolocations);

		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		assertThat(result.getAllErrors(), hasSize(1));
		Assert.assertEquals("observation.unit.import.plot.levelCode.exists", result.getAllErrors().get(0).getCode());
	}

	@Test
	public void testPruneObservationUnitsInvalidForImport_importDuplicatedPlotCode() {
		final StudyInstanceDto studyInstanceDto = new StudyInstanceDto();
		studyInstanceDto.setTrialDbId(TRIAL_DBID);
		studyInstanceDto.setProgramDbId(PROGRAM_DBID);
		studyInstanceDto.setStudyDbId(STUDY_DBID);
		Mockito.when(this.studyServiceBrapi.getStudyInstances(ArgumentMatchers.any(), ArgumentMatchers.isNull()))
			.thenReturn(Collections.singletonList(studyInstanceDto));

		Mockito.when(this.observationUnitService.getPlotObservationLevelRelationshipsByGeolocations(new HashSet<>(Arrays.asList(STUDY_DBID))))
			.thenReturn(new HashMap<>());

		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(GERMPLASM_DBID);
		Mockito.when(this.germplasmService.searchGermplasmDTO(ArgumentMatchers.any(), ArgumentMatchers.isNull()))
			.thenReturn(Collections.singletonList(germplasmDTO));

		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = new ArrayList();
		observationUnitImportRequestDtos.add(this.createObservationUnitImportRequestDto());
		observationUnitImportRequestDtos.add(this.createObservationUnitImportRequestDto());

		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		assertThat(result.getAllErrors(), hasSize(1));
		final ObjectError error = result.getAllErrors().get(0);
		Assert.assertEquals("observation.unit.import.plot.levelCode.duplicated", error.getCode());
		assertThat(error.getArguments().length, is(1));
		// Assert that second import object is the one that is failing
		assertThat(error.getArguments()[0], is("2"));
	}

	private List<ObservationUnitImportRequestDto> createObservationUnitImportRequestDtos() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = new ArrayList<>();
		observationUnitImportRequestDtos.add(this.createObservationUnitImportRequestDto());
		return observationUnitImportRequestDtos;
	}

	private ObservationUnitImportRequestDto createObservationUnitImportRequestDto() {
		return this.createObservationUnitImportRequestDto("1");
	}

	private ObservationUnitImportRequestDto createObservationUnitImportRequestDto(final String levelCode) {
		final ObservationUnitImportRequestDto dto = new ObservationUnitImportRequestDto();
		dto.setTrialDbId(TRIAL_DBID);
		dto.setProgramDbId(PROGRAM_DBID);
		dto.setStudyDbId(STUDY_DBID);
		dto.setGermplasmDbId(GERMPLASM_DBID);
		dto.setGermplasmDbId(GERMPLASM_DBID);

		dto.setObservationUnitPosition(this.createObservationUnitPosition(Collections.singletonList(this.createObservationLevelRelationship(levelCode))));
		return dto;
	}

	private ObservationUnitPosition createObservationUnitPosition(final List<ObservationLevelRelationship> observationLevelRelationships) {
		final ObservationUnitPosition observationUnitPosition = new ObservationUnitPosition();
		observationUnitPosition.setEntryType(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName());
		observationUnitPosition.setPositionCoordinateX("1");
		observationUnitPosition.setPositionCoordinateY("1");
		observationUnitPosition.setObservationLevelRelationships(observationLevelRelationships);
		return observationUnitPosition;
	}

	private ObservationLevelRelationship createObservationLevelRelationship(final String levelCode) {
		return new ObservationLevelRelationship(null, levelCode, "PLOT", null);
	}

}
