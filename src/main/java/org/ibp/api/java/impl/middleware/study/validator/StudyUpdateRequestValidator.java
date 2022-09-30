package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.TrialServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableTypeGroup;
import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.api.brapi.v2.study.StudyUpdateRequestDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StudyUpdateRequestValidator {

	private static final int MAX_REFERENCE_ID_LENGTH = 2000;
	private static final int MAX_REFERENCE_SOURCE_LENGTH = 255;
	private static final int MAX_ENVIRONMENT_PARAMETER_LENGTH = 255;

	@Autowired
	private TrialServiceBrapi trialServiceBrapi;

	@Autowired
	private LocationService locationService;

	@Autowired
	private VariableServiceBrapi variableServiceBrapi;

	protected BindingResult errors;

	public BindingResult validate(final Integer studyDbId, final StudyUpdateRequestDTO studyUpdateRequestDTO) {
		this.errors = new MapBindingResult(new HashMap<>(), StudyImportRequestDTO.class.getName());

		if (StringUtils.isEmpty(studyUpdateRequestDTO.getTrialDbId())) {
			this.errors.reject("study.update.trialDbId.required", "");
		}

		if (StringUtils.isNotEmpty(studyUpdateRequestDTO.getTrialDbId())) {
			final StudySearchFilter studySearchFilter = new StudySearchFilter();
			studySearchFilter.setTrialDbIds(Arrays.asList(studyUpdateRequestDTO.getTrialDbId()));
			final Map<String, StudySummary> trialsMap = this.trialServiceBrapi.getStudies(studySearchFilter, null).stream()
				.collect(Collectors.toMap(s -> String.valueOf(s.getTrialDbId()), Function.identity()));
			if (!trialsMap.containsKey(studyUpdateRequestDTO.getTrialDbId())) {
				this.errors.reject("study.update.trialDbId.invalid", "");
			} else if (trialsMap.get(studyUpdateRequestDTO.getTrialDbId()).getInstanceMetaData().stream()
				.noneMatch(m -> m.getInstanceDbId().equals(studyDbId))) {
				// Make sure the studyDbId specified is part of the trial.
				this.errors.reject("study.update.studyDbId.invalid", "");
			}
		}

		if (StringUtils.isNotEmpty(studyUpdateRequestDTO.getLocationDbId())) {
			final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
			locationSearchRequest.setLocationIds(Collections.singletonList(Integer.valueOf(studyUpdateRequestDTO.getLocationDbId())));
			if (CollectionUtils.isEmpty(this.locationService.searchLocations(locationSearchRequest, null, null))) {
				this.errors.reject("study.update.locationDbId.invalid", "");
			}
		}

		if (!CollectionUtils.isEmpty(studyUpdateRequestDTO.getSeasons()) && studyUpdateRequestDTO.getSeasons().size() > 1) {
			this.errors.reject("study.update.season.invalid", "");
		}

		this.validateObservationVariableDbIds(studyUpdateRequestDTO);

		this.validateExternelReferences(studyUpdateRequestDTO);

		this.validateEnvironmentParameters(studyUpdateRequestDTO);

		return this.errors;
	}

	private void validateObservationVariableDbIds(final StudyUpdateRequestDTO studyUpdateRequestDTO) {

		if (!CollectionUtils.isEmpty(studyUpdateRequestDTO.getObservationVariableDbIds())) {
			final VariableSearchRequestDTO variableSearchRequestDTO = new VariableSearchRequestDTO();
			variableSearchRequestDTO.setObservationVariableDbIds(studyUpdateRequestDTO.getObservationVariableDbIds());
			final Map<String, VariableDTO> variableDTOMap =
				this.variableServiceBrapi.getVariables(variableSearchRequestDTO, null, VariableTypeGroup.TRAIT).stream()
					.collect(Collectors.toMap(VariableDTO::getObservationVariableDbId, Function.identity()));

			for (final String observationVariableDbId : studyUpdateRequestDTO.getObservationVariableDbIds()) {
				if (!variableDTOMap.containsKey(observationVariableDbId)) {
					this.errors.reject("study.update.observationVariableDbId.invalid", new String[] {observationVariableDbId}, "");
				}
			}

		}

	}

	private void validateExternelReferences(final StudyUpdateRequestDTO studyUpdateRequestDTO) {
		if (studyUpdateRequestDTO.getExternalReferences() != null) {
			studyUpdateRequestDTO.getExternalReferences().forEach(r -> {
				if (r == null || StringUtils.isEmpty(r.getReferenceID()) || StringUtils.isEmpty(r.getReferenceSource())) {
					this.errors.reject("study.update.reference.null", "");
				}
				if (StringUtils.isNotEmpty(r.getReferenceID()) && r.getReferenceID().length() > MAX_REFERENCE_ID_LENGTH) {
					this.errors.reject("study.update.reference.id.exceeded.length", "");
				}
				if (StringUtils.isNotEmpty(r.getReferenceSource()) && r.getReferenceSource().length() > MAX_REFERENCE_SOURCE_LENGTH) {
					this.errors.reject("study.update.reference.source.exceeded.length", "");
				}
			});
		}
	}

	private void validateEnvironmentParameters(final StudyUpdateRequestDTO s) {
		if (!CollectionUtils.isEmpty(s.getEnvironmentParameters())) {
			s.getEnvironmentParameters().forEach(e -> {
				if (StringUtils.isEmpty(e.getParameterPUI())) {
					this.errors.reject("study.update.environment.parameter.pui.null", "");
				}
				if (StringUtils.isNotEmpty(e.getValue()) && e.getValue().length() > MAX_ENVIRONMENT_PARAMETER_LENGTH) {
					this.errors.reject("study.update.environment.parameter.value.exceeded.length", "");
				}
			});
		}
	}

}
