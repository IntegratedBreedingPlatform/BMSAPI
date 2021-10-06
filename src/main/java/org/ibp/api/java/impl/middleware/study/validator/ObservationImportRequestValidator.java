package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.StudyServiceBrapi;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmSearchRequest;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.brapi.GermplasmServiceBrapi;
import org.ibp.api.brapi.VariableServiceBrapi;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.observationunits.ObservationUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ObservationImportRequestValidator {

    private static final int MAX_REFERENCE_ID_LENGTH = 2000;
    private static final int MAX_REFERENCE_SOURCE_LENGTH = 255;
    private static final int MAX_VALUE_LENGTH = 255;

    @Autowired
    private GermplasmServiceBrapi germplasmServiceBrapi;

    @Autowired
    private StudyServiceBrapi studyServiceBrapi;

    @Autowired
    private ObservationUnitService observationUnitService;

    @Autowired
    private VariableServiceBrapi variableServiceBrapi;

    protected BindingResult errors;

    public BindingResult pruneObservationsInvalidForImport(final List<ObservationDto> observationDtos) {
        BaseValidator.checkNotEmpty(observationDtos, "observation.import.request.null");
        this.errors = new MapBindingResult(new HashMap<>(), ObservationDto.class.getName());

        final List<String> germplasmDbIds = observationDtos.stream().filter(o -> StringUtils.isNotEmpty(o.getGermplasmDbId()))
                .map(ObservationDto::getGermplasmDbId).collect(Collectors.toList());
        final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
        germplasmSearchRequest.setGermplasmDbIds(germplasmDbIds);
        final Map<String, GermplasmDTO> germplasmDTOMap = this.germplasmServiceBrapi.searchGermplasmDTO(germplasmSearchRequest, null)
                .stream().collect(Collectors.toMap(GermplasmDTO::getGermplasmDbId, Function.identity()));

        final List<String> studyDbIds = observationDtos.stream().filter(o -> StringUtils.isNotEmpty(o.getStudyDbId()))
                .map(ObservationDto::getStudyDbId).collect(Collectors.toList());
        final StudySearchFilter studySearchFilter = new StudySearchFilter();
        studySearchFilter.setStudyDbIds(studyDbIds);
        final Map<String, StudyInstanceDto> studyInstancesMap =
                this.studyServiceBrapi.getStudyInstances(studySearchFilter, null).stream()
                        .collect(Collectors.toMap(StudyInstanceDto::getStudyDbId, Function.identity()));

        final List<String> observationUnitDbIds = observationDtos.stream().filter(o -> StringUtils.isNotEmpty(o.getObservationUnitDbId()))
                .map(ObservationDto::getObservationUnitDbId).collect(Collectors.toList());
        final ObservationUnitSearchRequestDTO observationUnitSearchRequestDTO = new ObservationUnitSearchRequestDTO();
        observationUnitSearchRequestDTO.setObservationUnitDbIds(observationUnitDbIds);
        final Map<String, ObservationUnitDto> observationUnitDtoMap =
                this.observationUnitService.searchObservationUnits(null, null, observationUnitSearchRequestDTO).stream()
                        .collect(Collectors.toMap(ObservationUnitDto::getObservationUnitDbId, Function.identity()));

        final List<String> variableIds = observationDtos.stream().filter(o -> StringUtils.isNotEmpty(o.getObservationVariableDbId()))
                .map(ObservationDto::getObservationVariableDbId).collect(Collectors.toList());
        final VariableSearchRequestDTO variableSearchRequestDTO = new VariableSearchRequestDTO();
        variableSearchRequestDTO.setObservationVariableDbIds(variableIds);
        final Map<String, VariableDTO> variableDTOMap =
                this.variableServiceBrapi.getObservationVariables(null, variableSearchRequestDTO, null).stream()
                        .collect(Collectors.toMap(VariableDTO::getObservationVariableDbId, Function.identity()));

        final Map<String, List<String>> studyVariableIdsMap = new HashMap<>();

        Integer index = 1;
        final Iterator<ObservationDto> iterator = observationDtos.iterator();
        while (iterator.hasNext()) {
            final ObservationDto dto = iterator.next();
            if(StringUtils.isEmpty(dto.getGermplasmDbId())) {
                this.errors.reject("observation.import.germplasmDbId.required", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            if(StringUtils.isEmpty(dto.getObservationUnitDbId())) {
                this.errors.reject("observation.import.observationUnitDbId.required", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            if(StringUtils.isEmpty(dto.getObservationVariableDbId())) {
                this.errors.reject("observation.import.observationVariableDbId.required", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            if(StringUtils.isEmpty(dto.getStudyDbId())) {
                this.errors.reject("observation.import.studyDbId.required", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            if(StringUtils.isEmpty(dto.getValue())) {
                this.errors.reject("observation.import.value.required", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            if(!germplasmDTOMap.containsKey(dto.getGermplasmDbId())) {
                this.errors.reject("observation.import.germplasmDbId.invalid", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            if(!studyInstancesMap.containsKey(dto.getStudyDbId())) {
                this.errors.reject("observation.import.studyDbId.invalid", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            if(!observationUnitDtoMap.containsKey(dto.getObservationUnitDbId())) {
                this.errors.reject("observation.import.observationUnitDbId.invalid", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            final ObservationUnitDto obsUnit = observationUnitDtoMap.get(dto.getObservationUnitDbId());
            if(!obsUnit.getStudyDbId().equalsIgnoreCase(dto.getStudyDbId())
                    || !obsUnit.getGermplasmDbId().equalsIgnoreCase(dto.getGermplasmDbId()) ) {
                this.errors.reject("observation.import.no.observationUnit", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            if(!variableDTOMap.containsKey(dto.getObservationVariableDbId())) {
                this.errors.reject("observation.import.observationVariableDbId.invalid", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            if(!studyVariableIdsMap.containsKey(dto.getStudyDbId())) {
                variableSearchRequestDTO.setStudyDbId(Collections.singletonList(dto.getStudyDbId()));
                final List<VariableDTO> variableDTOS =  this.variableServiceBrapi.getObservationVariables(null, variableSearchRequestDTO, null);
                 List<String> studyVariableIds = new ArrayList<>();
                 if(!CollectionUtils.isEmpty(variableDTOS)) {
                     studyVariableIds = variableDTOS.stream().map(VariableDTO::getObservationVariableDbId)
                             .collect(Collectors.toList());
                 }
                studyVariableIdsMap.put(dto.getStudyDbId(), studyVariableIds);
            }
            if(!studyVariableIdsMap.get(dto.getStudyDbId()).contains(dto.getObservationVariableDbId())) {
                this.errors.reject("observation.import.observationVariableDbId.not.in.study", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            if(dto.getValue().length() > MAX_VALUE_LENGTH) {
                this.errors.reject("observation.import.value.exceeded.length", new String[] {index.toString()}, "");
                iterator.remove();
                continue;
            }
            if (this.isAnyExternalReferenceInvalid(dto, index)) {
                iterator.remove();
                continue;
            }
            index++;
        }

        return errors;
    }

    private boolean isAnyExternalReferenceInvalid(final ObservationDto dto, final Integer index) {
        if (dto.getExternalReferences() != null) {
            return dto.getExternalReferences().stream().anyMatch(r -> {
                if (r == null || StringUtils.isEmpty(r.getReferenceID()) || StringUtils.isEmpty(r.getReferenceSource())) {
                    this.errors.reject("observation.import.reference.null", new String[] {index.toString(), "externalReference"}, "");
                    return true;
                }
                if (StringUtils.isNotEmpty(r.getReferenceID()) && r.getReferenceID().length() > MAX_REFERENCE_ID_LENGTH) {
                    this.errors.reject("observation.import.reference.id.exceeded.length", new String[] {index.toString(), "referenceID"}, "");
                    return true;
                }
                if (StringUtils.isNotEmpty(r.getReferenceSource()) && r.getReferenceSource().length() > MAX_REFERENCE_SOURCE_LENGTH) {
                    this.errors.reject("observation.import.reference.source.exceeded.length", new String[] {index.toString(), "referenceSource"},
                            "");
                    return true;
                }
                return false;
            });
        }
        return false;
    }
}
