package org.ibp.api.rest.dataset.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.GermplasmServiceBrapi;
import org.generationcp.middleware.api.brapi.StudyServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmSearchRequest;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.java.impl.middleware.study.validator.ObservationImportRequestValidator;
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
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ObservationImportRequestValidatorTest {

    private static final String GERMPLASM_DBID = RandomStringUtils.randomAlphabetic(8);
    private static final String STUDY_DBID = "1";
    private static final String VARIABLE_DBID = RandomStringUtils.randomAlphabetic(5);
    private static final String VALUE = RandomStringUtils.randomNumeric(5);
    private static final String OBSERVATION_UNIT_DBID = RandomStringUtils.randomAlphabetic(5);

    @Mock
    private StudyServiceBrapi studyServiceBrapi;

    @Mock
    private GermplasmServiceBrapi germplasmService;

    @Mock
    private ObservationUnitService observationUnitService;

    @Mock
    private VariableServiceBrapi variableServiceBrapi;

    @InjectMocks
    private ObservationImportRequestValidator observationImportRequestValidator;

    @Before
    public void setUp() {
        final StudyInstanceDto studyInstanceDto = new StudyInstanceDto();
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

        final ObservationUnitDto observationUnitDto = new ObservationUnitDto();
        observationUnitDto.setGermplasmDbId(GERMPLASM_DBID);
        observationUnitDto.setObservationUnitDbId(OBSERVATION_UNIT_DBID);
        observationUnitDto.setStudyDbId(STUDY_DBID);
        final ObservationUnitSearchRequestDTO observationUnitSearchRequestDTO = new ObservationUnitSearchRequestDTO();
        observationUnitSearchRequestDTO.setObservationUnitDbIds(Collections.singletonList(OBSERVATION_UNIT_DBID));
        Mockito.when(this.observationUnitService.searchObservationUnits(null, null, observationUnitSearchRequestDTO))
                .thenReturn(Collections.singletonList(observationUnitDto));

        final VariableDTO variableDTO = new VariableDTO();
        variableDTO.setObservationVariableDbId(VARIABLE_DBID);
        variableDTO.getScale().setDataType(DataType.NUMERIC_VARIABLE.getBrapiName());
        final VariableSearchRequestDTO variableSearchRequestDTOUsingVariableId = new VariableSearchRequestDTO();
        variableSearchRequestDTOUsingVariableId.setObservationVariableDbIds(Collections.singletonList(VARIABLE_DBID));
        Mockito.when(this.variableServiceBrapi.getObservationVariables(variableSearchRequestDTOUsingVariableId, null))
                .thenReturn(Collections.singletonList(variableDTO));

        final VariableSearchRequestDTO variableSearchRequestDTOUsingStudyDbId = new VariableSearchRequestDTO();
        variableSearchRequestDTOUsingStudyDbId.setObservationVariableDbIds(Collections.singletonList(VARIABLE_DBID));
        variableSearchRequestDTOUsingStudyDbId.setStudyDbId(Collections.singletonList(STUDY_DBID));
        Mockito.when(this.variableServiceBrapi.getObservationVariables(variableSearchRequestDTOUsingStudyDbId, null))
                .thenReturn(Collections.singletonList(variableDTO));
    }

    @Test
    public void testPruneObservationInvalidForImport_Success() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertFalse(result.hasErrors());
    }

    @Test
    public void testPruneObservationInvalidForImport_NoGermplasmDbId() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setGermplasmDbId(null);
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.germplasmDbId.required", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_NonNumericValue() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setValue(RandomStringUtils.randomAlphabetic(5));
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.value.non.numeric", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_InvalidDateFormat() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final VariableDTO variableDTO = new VariableDTO();
        variableDTO.setObservationVariableDbId(VARIABLE_DBID);
        variableDTO.getScale().setDataType(DataType.DATE_TIME_VARIABLE.getBrapiName());
        final VariableSearchRequestDTO variableSearchRequestDTOUsingVariableId = new VariableSearchRequestDTO();
        variableSearchRequestDTOUsingVariableId.setObservationVariableDbIds(Collections.singletonList(VARIABLE_DBID));
        Mockito.when(this.variableServiceBrapi.getObservationVariables(variableSearchRequestDTOUsingVariableId, null))
                .thenReturn(Collections.singletonList(variableDTO));
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.value.invalid.date", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testruneObservationInvalidForImport_NoStudyDbId() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setStudyDbId(null);
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.studyDbId.required", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_NoVariableDbId() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setObservationVariableDbId(null);
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.observationVariableDbId.required", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_NoObservationUnitDbId() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setObservationUnitDbId(null);
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.observationUnitDbId.required", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_GermplasmDbIdInvalid() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        Mockito.when(this.germplasmService.searchGermplasmDTO(ArgumentMatchers.any(GermplasmSearchRequest.class), ArgumentMatchers.eq(null)))
                .thenReturn(new ArrayList<>());
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.germplasmDbId.invalid", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_StudyDbIdInvalid() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        Mockito.when(this.studyServiceBrapi.getStudyInstances(ArgumentMatchers.any(StudySearchFilter.class), ArgumentMatchers.eq(null)))
                .thenReturn(new ArrayList<>());
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.studyDbId.invalid", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_VariableDbIdInvalid() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        Mockito.when(this.variableServiceBrapi.getObservationVariables(ArgumentMatchers.any(VariableSearchRequestDTO.class), ArgumentMatchers.eq(null)))
                .thenReturn(new ArrayList<>());
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.observationVariableDbId.invalid", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_ObservationUnitDbIdInvalid() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        Mockito.when(this.observationUnitService.searchObservationUnits(ArgumentMatchers.eq(null), ArgumentMatchers.eq(null), ArgumentMatchers.any(ObservationUnitSearchRequestDTO.class)))
                .thenReturn(new ArrayList<>());
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.observationUnitDbId.invalid", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_NoValue() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setValue(null);
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.value.required", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_NoCorrespondingObservationUnit() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final ObservationUnitDto observationUnitDto = new ObservationUnitDto();
        observationUnitDto.setGermplasmDbId(RandomStringUtils.randomAlphabetic(5));
        observationUnitDto.setObservationUnitDbId(OBSERVATION_UNIT_DBID);
        observationUnitDto.setStudyDbId(STUDY_DBID);
        final ObservationUnitSearchRequestDTO observationUnitSearchRequestDTO = new ObservationUnitSearchRequestDTO();
        observationUnitSearchRequestDTO.setObservationUnitDbIds(Collections.singletonList(OBSERVATION_UNIT_DBID));
        Mockito.when(this.observationUnitService.searchObservationUnits(null, null, observationUnitSearchRequestDTO))
                .thenReturn(Collections.singletonList(observationUnitDto));
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.no.observationUnit", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_ValueExceedsLength() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setValue(RandomStringUtils.randomAlphabetic(256));
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.value.exceeded.length", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_ObservationVariableDbIdNotInStudy() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final VariableSearchRequestDTO variableSearchRequestDTOUsingStudyDbId = new VariableSearchRequestDTO();
        variableSearchRequestDTOUsingStudyDbId.setObservationVariableDbIds(Collections.singletonList(VARIABLE_DBID));
        variableSearchRequestDTOUsingStudyDbId.setStudyDbId(Collections.singletonList(STUDY_DBID));
        Mockito.when(this.variableServiceBrapi.getObservationVariables(variableSearchRequestDTOUsingStudyDbId, null))
                .thenReturn(new ArrayList<>());
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.observationVariableDbId.not.in.study", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_WhereExternalReferenceHasMissingInfo() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
        externalReferenceDTOS.add(new ExternalReferenceDTO());
        observationDtos.get(0).setExternalReferences(externalReferenceDTOS);
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.reference.null", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_WhereExternalReferenceIdExceedsLength() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
        final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
        externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(2001));
        externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(200));
        externalReferenceDTOS.add(externalReferenceDTO);
        observationDtos.get(0).setExternalReferences(externalReferenceDTOS);
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.reference.id.exceeded.length", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForImport_WhereExternalReferenceSourceExceedsLength() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
        final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
        externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(200));
        externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(256));
        externalReferenceDTOS.add(externalReferenceDTO);
        observationDtos.get(0).setExternalReferences(externalReferenceDTOS);
        final BindingResult result = this.observationImportRequestValidator.pruneObservationsInvalidForImport(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.reference.source.exceeded.length", result.getAllErrors().get(0).getCode());
    }


    private List<ObservationDto> createObservationDtoList() {
        final List<ObservationDto> observationDtos = new ArrayList<>();
        final ObservationDto observationDto = new ObservationDto();
        observationDto.setStudyDbId(STUDY_DBID);
        observationDto.setGermplasmDbId(GERMPLASM_DBID);
        observationDto.setObservationVariableDbId(VARIABLE_DBID);
        observationDto.setValue(VALUE);
        observationDto.setObservationUnitDbId(OBSERVATION_UNIT_DBID);
        observationDtos.add(observationDto);
        return observationDtos;
    }
}
