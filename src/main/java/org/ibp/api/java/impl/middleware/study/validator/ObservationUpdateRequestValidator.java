package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.ObservationServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableTypeGroup;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationSearchRequestDto;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.study.ScaleDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.generationcp.middleware.util.Util;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ObservationUpdateRequestValidator {
    private static final int MAX_REFERENCE_ID_LENGTH = 2000;
    private static final int MAX_REFERENCE_SOURCE_LENGTH = 255;
    private static final int MAX_VALUE_LENGTH = 255;

    protected BindingResult errors;

    @Autowired
    private ObservationServiceBrapi observationServiceBrapi;

    @Autowired
    private VariableServiceBrapi variableServiceBrapi;

    public BindingResult pruneObservationsInvalidForUpdate(final List<ObservationDto> observationDtos) {
        BaseValidator.checkNotEmpty(observationDtos, "observation.import.request.null");
        this.errors = new MapBindingResult(new HashMap<>(), ObservationDto.class.getName());

        final ObservationSearchRequestDto observationSearchRequestDto = new ObservationSearchRequestDto();
        observationSearchRequestDto.setObservationDbIds(observationDtos.stream().filter(obs -> NumberUtils.isNumber(obs.getObservationDbId()))
                .map(obs -> Integer.valueOf(obs.getObservationDbId())).collect(Collectors.toList()));
        final Map<String, ObservationDto> existingObservations = this.observationServiceBrapi.searchObservations(observationSearchRequestDto, null)
                .stream().collect(Collectors.toMap(ObservationDto::getObservationDbId, Function.identity()));

        final List<String> variableIds = observationDtos.stream().filter(o -> StringUtils.isNotEmpty(o.getObservationVariableDbId()))
                .map(ObservationDto::getObservationVariableDbId).collect(Collectors.toList());
        final VariableSearchRequestDTO variableSearchRequestDTO = new VariableSearchRequestDTO();
        variableSearchRequestDTO.setObservationVariableDbIds(variableIds);
        final Map<String, VariableDTO> variableDTOMap =
                this.variableServiceBrapi.getVariables(variableSearchRequestDTO, null, VariableTypeGroup.TRAIT).stream()
                        .collect(Collectors.toMap(VariableDTO::getObservationVariableDbId, Function.identity()));

        final Map<ObservationDto, Integer> importRequestByIndexMap = IntStream.range(0, observationDtos.size())
                .boxed().collect(Collectors.toMap(observationDtos::get, i -> i));

        observationDtos.removeIf(dto -> {
            final Integer index = importRequestByIndexMap.get(dto) + 1;

            return this.hasNoExistingObservation(dto, existingObservations, index) ||
                    this.isGermplasmDbIdInvalidForUpdate(dto, existingObservations.get(dto.getObservationDbId()), index) ||
                    this.isObservationUnitDbIdInvalidForUpdate(dto, existingObservations.get(dto.getObservationDbId()), index) ||
                    this.isObservationVariableDbIdInvalidForUpdate(dto, existingObservations.get(dto.getObservationDbId()), index) ||
                    this.isStudyDbIdInvalidForUpdate(dto, existingObservations.get(dto.getObservationDbId()), index) ||
                    this.isValueInvalid(dto, variableDTOMap, index) ||
                    this.isAnyExternalReferenceInvalid(dto, index);
        });
        return this.errors;
    }

    private boolean hasNoExistingObservation(final ObservationDto dto, final Map<String, ObservationDto> observationDtoMap, final Integer index) {
        if (!observationDtoMap.containsKey(dto.getObservationDbId())) {
            this.errors.reject("observation.update.no.observation", new String[] {index.toString()}, "");
            return true;
        }
        return false;
    }

    private boolean isGermplasmDbIdInvalidForUpdate(final ObservationDto dto, final ObservationDto existingObservation, final Integer index) {
        if (StringUtils.isNotEmpty(dto.getGermplasmDbId()) && !dto.getGermplasmDbId().equalsIgnoreCase(existingObservation.getGermplasmDbId())) {
            this.errors.reject("observation.update.germplasmDbId.invalid", new String[] {index.toString()}, "");
            return true;
        }
        return false;
    }

    private boolean isObservationUnitDbIdInvalidForUpdate(final ObservationDto dto, final ObservationDto existingObservation, final Integer index) {
        if (StringUtils.isEmpty(dto.getObservationUnitDbId())) {
            this.errors.reject("observation.import.observationUnitDbId.required", new String[] {index.toString()}, "");
            return true;
        }
        if (!dto.getObservationUnitDbId().equalsIgnoreCase(existingObservation.getObservationUnitDbId())) {
            this.errors.reject("observation.update.observationUnitDbId.invalid", new String[] {index.toString()}, "");
            return true;
        }
        return false;
    }

    private boolean isObservationVariableDbIdInvalidForUpdate(final ObservationDto dto, final ObservationDto existingObservation, final Integer index) {
        if (StringUtils.isEmpty(dto.getObservationVariableDbId())) {
            this.errors.reject("observation.import.observationVariableDbId.required", new String[] {index.toString()}, "");
            return true;
        }
        if (!dto.getObservationVariableDbId().equals(existingObservation.getObservationVariableDbId())) {
            this.errors.reject("observation.update.observationVariableDbId.invalid", new String[] {index.toString()}, "");
            return true;
        }
        return false;
    }

    private boolean isStudyDbIdInvalidForUpdate(final ObservationDto dto, final ObservationDto existingObservation, final Integer index) {
        if (StringUtils.isNotEmpty(dto.getStudyDbId()) && !dto.getStudyDbId().equals(existingObservation.getStudyDbId())) {
            this.errors.reject("observation.update.studyDbId.invalid", new String[] {index.toString()}, "");
            return true;
        }
        return false;
    }

    private boolean isValueInvalid(final ObservationDto dto, final Map<String, VariableDTO> variableDTOMap, final Integer index) {
        if (StringUtils.isEmpty(dto.getValue())) {
            this.errors.reject("observation.import.value.required", new String[] {index.toString()}, "");
            return true;
        }
        if (dto.getValue().length() > MAX_VALUE_LENGTH) {
            this.errors.reject("observation.import.value.exceeded.length", new String[] {index.toString()}, "");
            return true;
        }
        final ScaleDTO scale = variableDTOMap.get(dto.getObservationVariableDbId()).getScale();
        if (DataType.NUMERIC_VARIABLE.getBrapiName().equalsIgnoreCase(scale.getDataType()) && !NumberUtils.isNumber(dto.getValue())) {
            this.errors.reject("observation.import.value.non.numeric", new String[] {index.toString()}, "");
            return true;
        }
        if (DataType.DATE_TIME_VARIABLE.getBrapiName().equalsIgnoreCase(scale.getDataType())
                && Util.tryParseDate(dto.getValue(), Util.DATE_AS_NUMBER_FORMAT) == null) {
            this.errors.reject("observation.import.value.invalid.date", new String[] {index.toString()}, "");
            return true;
        }
        return false;
    }

    private boolean isAnyExternalReferenceInvalid(final ObservationDto dto, final Integer index) {
        if (dto.getExternalReferences() != null) {
            return dto.getExternalReferences().stream().anyMatch(r -> {
                if (r == null || StringUtils.isEmpty(r.getReferenceID()) || StringUtils.isEmpty(r.getReferenceSource())) {
                    this.errors.reject("observation.import.reference.null", new String[] {index.toString(), "externalReference"}, "");
                    return true;
                }
                if (StringUtils.isNotEmpty(r.getReferenceID()) && r.getReferenceID().length() > MAX_REFERENCE_ID_LENGTH) {
                    this.errors
                            .reject("observation.import.reference.id.exceeded.length", new String[] {index.toString(), "referenceID"}, "");
                    return true;
                }
                if (StringUtils.isNotEmpty(r.getReferenceSource()) && r.getReferenceSource().length() > MAX_REFERENCE_SOURCE_LENGTH) {
                    this.errors
                            .reject("observation.import.reference.source.exceeded.length", new String[] {index.toString(), "referenceSource"},
                                    "");
                    return true;
                }
                return false;
            });
        }
        return false;
    }
}
