package org.ibp.api.rest.dataset.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.ObservationServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableTypeGroup;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationSearchRequestDto;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.java.impl.middleware.study.validator.ObservationUpdateRequestValidator;
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
import java.util.concurrent.ThreadLocalRandom;

@RunWith(MockitoJUnitRunner.class)
public class ObservationUpdateRequestValidatorTest {

    private static final String GERMPLASM_DBID = RandomStringUtils.randomAlphabetic(8);
    private static final String OBSERVATION_DBID = RandomStringUtils.randomNumeric(5);
    private static final String STUDY_DBID = "1";
    private static final String VARIABLE_DBID = RandomStringUtils.randomNumeric(5);
    private static final String VALUE = RandomStringUtils.randomNumeric(5);
    private static final String OBSERVATION_UNIT_DBID = RandomStringUtils.randomAlphabetic(5);

    @Mock
    private ObservationServiceBrapi observationServiceBrapi;

    @Mock
    private VariableServiceBrapi variableServiceBrapi;

    @InjectMocks
    private ObservationUpdateRequestValidator observationUpdateRequestValidator;

    @Before
    public void setUp() {
        final VariableDTO variableDTO = new VariableDTO();
        variableDTO.setObservationVariableDbId(VARIABLE_DBID);
        variableDTO.getScale().setDataType(DataType.NUMERIC_VARIABLE.getBrapiName());
        variableDTO.getContextOfUse().add(VariableDTO.ContextOfUseEnum.PLOT.name());
        final VariableSearchRequestDTO variableSearchRequestDTOUsingVariableId = new VariableSearchRequestDTO();
        variableSearchRequestDTOUsingVariableId.setObservationVariableDbIds(Collections.singletonList(VARIABLE_DBID));
        Mockito.when(this.variableServiceBrapi.getVariables(variableSearchRequestDTOUsingVariableId, null, VariableTypeGroup.TRAIT))
                .thenReturn(Collections.singletonList(variableDTO));

        final ObservationSearchRequestDto observationSearchRequestDto = new ObservationSearchRequestDto();
        observationSearchRequestDto.setObservationDbIds(Collections.singletonList(OBSERVATION_DBID));
        Mockito.when(this.observationServiceBrapi.searchObservations(observationSearchRequestDto, null)).thenReturn(this.createObservationDtoList());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_Success() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertFalse(result.hasErrors());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_WhereObservationNotExisting () {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setObservationDbId(RandomStringUtils.randomNumeric(5));
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.update.no.observation", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_WhereGermplasmDbIdInvalid () {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setGermplasmDbId(RandomStringUtils.randomAlphabetic(5));
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.update.germplasmDbId.invalid", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_WhereObservationUnitDbIdInvalid () {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setObservationUnitDbId(RandomStringUtils.randomAlphabetic(5));
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.update.observationUnitDbId.invalid", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_WhereObservationUnitDbIdNotSpecified () {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setObservationUnitDbId(null);
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.observationUnitDbId.required", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_WhereObservationVariableDbIdInvalid () {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setObservationVariableDbId(RandomStringUtils.randomAlphabetic(5));
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.update.observationVariableDbId.invalid", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_WhereObservationVariableDbIdNotSpecified () {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setObservationVariableDbId(null);
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.observationVariableDbId.required", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_WhereStudyDbIdInvalid () {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setStudyDbId(RandomStringUtils.randomAlphabetic(5));
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.update.studyDbId.invalid", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_NonNumericValue() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setValue(RandomStringUtils.randomAlphabetic(5));
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.value.non.numeric", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationValidForUpdate_NumericDecimalValue() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setValue(String.valueOf(ThreadLocalRandom.current().nextDouble(0, 1000)));
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertFalse(result.hasErrors());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_InvalidDateFormat() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final VariableDTO variableDTO = new VariableDTO();
        variableDTO.setObservationVariableDbId(VARIABLE_DBID);
        variableDTO.getScale().setDataType(DataType.DATE_TIME_VARIABLE.getBrapiName());
        final VariableSearchRequestDTO variableSearchRequestDTOUsingVariableId = new VariableSearchRequestDTO();
        variableSearchRequestDTOUsingVariableId.setObservationVariableDbIds(Collections.singletonList(VARIABLE_DBID));
        Mockito.when(this.variableServiceBrapi.getVariables(variableSearchRequestDTOUsingVariableId, null, VariableTypeGroup.TRAIT))
                .thenReturn(Collections.singletonList(variableDTO));
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.value.invalid.date", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_ValueExceedsLength() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        observationDtos.get(0).setValue(RandomStringUtils.randomAlphabetic(256));
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.value.exceeded.length", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_WhereExternalReferenceHasMissingInfo() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
        externalReferenceDTOS.add(new ExternalReferenceDTO());
        observationDtos.get(0).setExternalReferences(externalReferenceDTOS);
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.reference.null", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_WhereExternalReferenceIdExceedsLength() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
        final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
        externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(2001));
        externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(200));
        externalReferenceDTOS.add(externalReferenceDTO);
        observationDtos.get(0).setExternalReferences(externalReferenceDTOS);
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals("observation.import.reference.id.exceeded.length", result.getAllErrors().get(0).getCode());
    }

    @Test
    public void testPruneObservationInvalidForUpdate_WhereExternalReferenceSourceExceedsLength() {
        final List<ObservationDto> observationDtos = this.createObservationDtoList();
        final List<ExternalReferenceDTO> externalReferenceDTOS = new ArrayList<>();
        final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
        externalReferenceDTO.setReferenceID(RandomStringUtils.randomAlphabetic(200));
        externalReferenceDTO.setReferenceSource(RandomStringUtils.randomAlphabetic(256));
        externalReferenceDTOS.add(externalReferenceDTO);
        observationDtos.get(0).setExternalReferences(externalReferenceDTOS);
        final BindingResult result = this.observationUpdateRequestValidator.pruneObservationsInvalidForUpdate(observationDtos);
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
        observationDto.setObservationDbId(OBSERVATION_DBID);
        observationDtos.add(observationDto);
        return observationDtos;
    }
}
