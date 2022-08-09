package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.GermplasmServiceBrapi;
import org.generationcp.middleware.api.brapi.ObservationServiceBrapi;
import org.generationcp.middleware.api.brapi.StudyServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableTypeGroup;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationSearchRequestDto;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmSearchRequest;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.generationcp.middleware.service.api.study.ScaleDTO;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.generationcp.middleware.util.Util;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ObservationImportRequestValidator {

	private static final int MAX_REFERENCE_ID_LENGTH = 2000;
	private static final int MAX_REFERENCE_SOURCE_LENGTH = 255;
	private static final int MAX_VALUE_LENGTH = 255;

	@Autowired
	private GermplasmServiceBrapi germplasmService;

	@Autowired
	private StudyServiceBrapi studyServiceBrapi;

	@Autowired
	private ObservationUnitService observationUnitService;

	@Autowired
	private ObservationServiceBrapi observationServiceBrapi;

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
		final Map<String, GermplasmDTO> germplasmDTOMap = this.germplasmService.searchGermplasmDTO(germplasmSearchRequest, null)
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
			this.variableServiceBrapi.getVariables(variableSearchRequestDTO, null, VariableTypeGroup.TRAIT).stream()
				.collect(Collectors.toMap(VariableDTO::getObservationVariableDbId, Function.identity()));

		final ObservationSearchRequestDto observationSearchRequestDto = new ObservationSearchRequestDto();
		observationSearchRequestDto.setObservationUnitDbIds(observationUnitDbIds);
		observationSearchRequestDto.setObservationVariableDbIds(variableIds);

		final List<ObservationDto> existingObservations =
			this.observationServiceBrapi.searchObservations(observationSearchRequestDto, null);
		final Map<String, Map<String, ObservationDto>> existingObservationsMap = new HashMap<>();
		if(CollectionUtils.isNotEmpty(existingObservations)) {
			for (final ObservationDto existingObservation : existingObservations) {
				final String observationUnitDbId = existingObservation.getObservationUnitDbId();
				existingObservationsMap.putIfAbsent(observationUnitDbId, new HashMap<>());
				existingObservationsMap.get(observationUnitDbId).put(existingObservation.getObservationVariableDbId(), existingObservation);
			}
		}

		final Map<String, List<String>> studyVariableIdsMap = new HashMap<>();

		final Map<ObservationDto, Integer> importRequestByIndexMap = IntStream.range(0, observationDtos.size())
			.boxed().collect(Collectors.toMap(observationDtos::get, i -> i));
		observationDtos.removeIf(dto -> {
			final Integer index = importRequestByIndexMap.get(dto) + 1;
			return this.isGermplasmDbIdInvalid(germplasmDTOMap, dto, index) ||
				this.isObservationUnitDbIdInvalid(observationUnitDtoMap, dto, index) ||
				this.isObservationVariableDbIdInvalid(variableDTOMap, dto, index) ||
				this.isStudyDbIdInvalid(studyInstancesMap, dto, index) ||
				this.hasNoExistingObservationUnit(observationUnitDtoMap, dto, index) ||
				this.isValueInvalid(dto, variableDTOMap, index) ||
				this.isObservationAlreadyExisting(dto, existingObservationsMap, index) ||
				this.isAnyExternalReferenceInvalid(dto, index);
		});

		return this.errors;
	}

	private boolean isObservationAlreadyExisting(final ObservationDto dto,
		final Map<String, Map<String, ObservationDto>> existingObservationsMap, final Integer index) {
		if(existingObservationsMap.containsKey(dto.getObservationUnitDbId())
			&& existingObservationsMap.get(dto.getObservationUnitDbId()).containsKey(dto.getObservationVariableDbId())) {
			this.errors.reject("observation.import.already.existing",
				new String[] {index.toString(), dto.getObservationUnitDbId(), dto.getObservationVariableDbId()}, "");
			return true;
		}
		return false;
	}

	private boolean hasNoExistingObservationUnit(
		final Map<String, ObservationUnitDto> observationUnitDtoMap, final ObservationDto dto, final Integer index) {
		final ObservationUnitDto obsUnit = observationUnitDtoMap.get(dto.getObservationUnitDbId());
		if ((!StringUtils.isEmpty(dto.getStudyDbId()) && !obsUnit.getStudyDbId().equalsIgnoreCase(dto.getStudyDbId()))
			|| (!StringUtils.isEmpty(dto.getGermplasmDbId()) && !obsUnit.getGermplasmDbId().equalsIgnoreCase(dto.getGermplasmDbId()))) {
			this.errors.reject("observation.import.no.observationUnit", new String[] {index.toString()}, "");
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

	private boolean isStudyDbIdInvalid(
		final Map<String, StudyInstanceDto> studyInstancesMap, final ObservationDto dto, final Integer index) {
		if (!StringUtils.isEmpty(dto.getStudyDbId()) && !studyInstancesMap.containsKey(dto.getStudyDbId())) {
			this.errors.reject("observation.import.studyDbId.invalid", new String[] {index.toString()}, "");
			return true;
		}
		return false;
	}

	private boolean isObservationVariableDbIdInvalid(
		final Map<String, VariableDTO> variableDTOMap, final ObservationDto dto, final Integer index) {
		if (StringUtils.isEmpty(dto.getObservationVariableDbId())) {
			this.errors.reject("observation.import.observationVariableDbId.required", new String[] {index.toString()}, "");
			return true;
		}
		if (!variableDTOMap.containsKey(dto.getObservationVariableDbId())) {
			this.errors.reject("observation.import.observationVariableDbId.invalid", new String[] {index.toString()}, "");
			return true;
		}
		return false;
	}

	private boolean isObservationUnitDbIdInvalid(
		final Map<String, ObservationUnitDto> observationUnitDtoMap, final ObservationDto dto, final Integer index) {
		if (StringUtils.isEmpty(dto.getObservationUnitDbId())) {
			this.errors.reject("observation.import.observationUnitDbId.required", new String[] {index.toString()}, "");
			return true;
		}
		if (!observationUnitDtoMap.containsKey(dto.getObservationUnitDbId())) {
			this.errors.reject("observation.import.observationUnitDbId.invalid", new String[] {index.toString()}, "");
			return true;
		}
		return false;
	}

	private boolean isGermplasmDbIdInvalid(final Map<String, GermplasmDTO> germplasmDTOMap, final ObservationDto dto, final Integer index) {
		if (!StringUtils.isEmpty(dto.getGermplasmDbId()) && !germplasmDTOMap.containsKey(dto.getGermplasmDbId())) {
			this.errors.reject("observation.import.germplasmDbId.invalid", new String[] {index.toString()}, "");
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
