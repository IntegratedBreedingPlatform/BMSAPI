package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.TrialServiceBrapi;
import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.api.brapi.v2.study.StudyUpdateRequestDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.api.ontology.OntologyVariableService;
import org.generationcp.middleware.domain.dms.TrialSummary;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.search_request.brapi.v2.TrialSearchRequestDTO;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
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
	private OntologyVariableService ontologyVariableService;

	protected BindingResult errors;

	public BindingResult validate(final Integer studyDbId, final StudyUpdateRequestDTO studyUpdateRequestDTO) {
		this.errors = new MapBindingResult(new HashMap<>(), StudyImportRequestDTO.class.getName());

		if (StringUtils.isEmpty(studyUpdateRequestDTO.getTrialDbId())) {
			this.errors.reject("study.update.trialDbId.required", "");
		}

		if (StringUtils.isNotEmpty(studyUpdateRequestDTO.getTrialDbId())) {
			// Find out if trialDbId is existing
			final TrialSearchRequestDTO trialSearchRequestDTO = new TrialSearchRequestDTO();
			trialSearchRequestDTO.setTrialDbIds(Arrays.asList(studyUpdateRequestDTO.getTrialDbId()));
			final Map<String, TrialSummary> trialsMap = this.trialServiceBrapi.searchTrials(trialSearchRequestDTO, null).stream()
				.collect(Collectors.toMap(s -> String.valueOf(s.getTrialDbId()), Function.identity()));
			if (!trialsMap.containsKey(studyUpdateRequestDTO.getTrialDbId())) {
				this.errors.reject("study.update.trialDbId.invalid", "");
			} else if (trialsMap.get(studyUpdateRequestDTO.getTrialDbId()).getInstanceMetaData().stream()
				.noneMatch(m -> m.getInstanceDbId().equals(studyDbId))) {
				// Make sure the studyDbId specified is associated to the trial.
				this.errors.reject("study.update.studyDbId.invalid", "");
			}
		}

		if (StringUtils.isNotEmpty(studyUpdateRequestDTO.getLocationDbId())) {
			final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
			locationSearchRequest.setLocationDbIds(Collections.singletonList(Integer.valueOf(studyUpdateRequestDTO.getLocationDbId())));
			// Find out if the specified locationDbId is existing
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
			final VariableFilter variableFilter = new VariableFilter();
			studyUpdateRequestDTO.getObservationVariableDbIds().stream().forEach(observationVariableId -> {
				variableFilter.addVariableId(Integer.valueOf(observationVariableId));
			});
			variableFilter.addVariableType(VariableType.TRAIT);
			variableFilter.addVariableType(VariableType.SELECTION_METHOD);
			final Map<Integer, Variable> existingVariables =
				this.ontologyVariableService.getVariablesWithFilterById(variableFilter);

			for (final String observationVariableDbId : studyUpdateRequestDTO.getObservationVariableDbIds()) {
				if (!existingVariables.containsKey(Integer.valueOf(observationVariableDbId))) {
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

			final VariableFilter variableFilter = new VariableFilter();
			s.getEnvironmentParameters().forEach(e -> {
				if (StringUtils.isNotEmpty(e.getParameterPUI())) {
					variableFilter.addVariableId(Integer.valueOf(e.getParameterPUI()));
				}
			});
			variableFilter.addVariableType(VariableType.ENVIRONMENT_DETAIL);
			variableFilter.addVariableType(VariableType.ENVIRONMENT_CONDITION);
			final Map<Integer, Variable> existingVariables =
				this.ontologyVariableService.getVariablesWithFilterById(variableFilter);

			s.getEnvironmentParameters().forEach(e -> {
				if (StringUtils.isEmpty(e.getParameterPUI())) {
					this.errors.reject("study.update.environment.parameter.pui.null", "");
					return;
				}
				if (StringUtils.isNotEmpty(e.getValue()) && e.getValue().length() > MAX_ENVIRONMENT_PARAMETER_LENGTH) {
					this.errors.reject("study.update.environment.parameter.value.exceeded.length", "");
					return;
				}
				if (!existingVariables.containsKey(Integer.valueOf(e.getParameterPUI()))) {
					this.errors.reject("study.update.environment.parameter.pui.invalid", new String[] {e.getParameterPUI()}, "");
					return;
				}
			});

		}
	}

}
