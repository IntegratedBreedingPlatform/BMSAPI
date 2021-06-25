package org.ibp.api.rest.dataset.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportRequestDto;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.pojos.Location;
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
	private static final String LOCATION_DBID = "1";

	@Mock
	private StudyInstanceService studyInstanceService;

	@Mock
	private LocationService locationService;

	@Mock
	private GermplasmService germplasmService;

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

		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		locationSearchRequest.setLocationIds(Collections.singletonList(Integer.valueOf(LOCATION_DBID)));
		locationSearchRequest.setProgramUUID(PROGRAM_DBID);
		Mockito.when(this.locationService.getFilteredLocations(locationSearchRequest, null))
			.thenReturn(Collections.singletonList(new Location()));

		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(GERMPLASM_DBID);
		final GermplasmSearchRequestDto germplasmSearchRequestDto = new GermplasmSearchRequestDto();
		germplasmSearchRequestDto.setGermplasmDbIds(Collections.singletonList(GERMPLASM_DBID));
		Mockito.when(this.germplasmService.searchFilteredGermplasm(germplasmSearchRequestDto, null))
			.thenReturn(Collections.singletonList(germplasmDTO));
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

	private List<ObservationUnitImportRequestDto> createObservationUnitImportRequestDtos() {
		final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos = new ArrayList<>();
		final ObservationUnitImportRequestDto dto = new ObservationUnitImportRequestDto();
		dto.setTrialDbId(TRIAL_DBID);
		dto.setProgramDbId(PROGRAM_DBID);
		dto.setStudyDbId(STUDY_DBID);
		dto.setGermplasmDbId(GERMPLASM_DBID);
		dto.setGermplasmDbId(GERMPLASM_DBID);
		dto.setLocationDbId(LOCATION_DBID);
		observationUnitImportRequestDtos.add(dto);
		return observationUnitImportRequestDtos;
	}
}
