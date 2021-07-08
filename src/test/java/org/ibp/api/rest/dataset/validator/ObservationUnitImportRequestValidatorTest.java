package org.ibp.api.rest.dataset.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationLevelRelationship;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportRequestDto;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitPosition;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.ibp.api.java.impl.middleware.observationunits.ObservationUnitImportRequestValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ObservationUnitImportRequestValidatorTest {

	private static final String GERMPLASM_DBID = RandomStringUtils.randomAlphabetic(8);
	private static final String TRIAL_DBID = "1";
	private static final String STUDY_DBID = "1";
	private static final String PROGRAM_DBID = RandomStringUtils.randomAlphabetic(10);

	@Mock
	private StudyInstanceService studyInstanceService;

	@Mock
	private GermplasmService germplasmService;

	@Mock
	private OntologyService ontologyService;

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
		Mockito.when(this.studyInstanceService.getStudyInstances(filter, null))
			.thenReturn(Collections.singletonList(studyInstanceDto));

		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(GERMPLASM_DBID);
		final GermplasmSearchRequestDto germplasmSearchRequestDto = new GermplasmSearchRequestDto();
		germplasmSearchRequestDto.setGermplasmDbIds(Collections.singletonList(GERMPLASM_DBID));
		Mockito.when(this.germplasmService.searchFilteredGermplasm(germplasmSearchRequestDto, null))
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
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
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
	public void testPruneStudiesInvalidForImport_WherePlotNoIsNotIncluded() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = this.createObservationUnitImportRequestDtos();
		observationUnitImportRequestDtos.get(0).getObservationUnitPosition().setObservationLevelRelationships(null);
		final BindingResult result = this.validator.pruneObservationUnitsInvalidForImport(observationUnitImportRequestDtos);
		Assert.assertTrue(result.hasErrors());
		Assert.assertEquals("observation.unit.import.no.plot.no", result.getAllErrors().get(0).getCode());
	}

	private List<ObservationUnitImportRequestDto> createObservationUnitImportRequestDtos() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = new ArrayList<>();
		final ObservationUnitImportRequestDto dto = new ObservationUnitImportRequestDto();
		dto.setTrialDbId(TRIAL_DBID);
		dto.setProgramDbId(PROGRAM_DBID);
		dto.setStudyDbId(STUDY_DBID);
		dto.setGermplasmDbId(GERMPLASM_DBID);
		dto.setGermplasmDbId(GERMPLASM_DBID);

		final ObservationUnitPosition observationUnitPosition = new ObservationUnitPosition();
		observationUnitPosition.setEntryType(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName());
		observationUnitPosition.setPositionCoordinateX("1");
		observationUnitPosition.setPositionCoordinateY("1");
		final ObservationLevelRelationship relationship =  new ObservationLevelRelationship("1", "PLOT_NO", null);
		observationUnitPosition.setObservationLevelRelationships(Collections.singletonList(relationship));
		dto.setObservationUnitPosition(observationUnitPosition);


		observationUnitImportRequestDtos.add(dto);
		return observationUnitImportRequestDtos;
	}
}
