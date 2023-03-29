package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.TrialServiceBrapi;
import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.dms.TrialSummary;
import org.generationcp.middleware.domain.search_request.brapi.v2.TrialSearchRequestDTO;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StudyImportRequestValidator {

	private static final int MAX_REFERENCE_ID_LENGTH = 2000;
	private static final int MAX_REFERENCE_SOURCE_LENGTH = 255;
	private static final int MAX_ENVIRONMENT_PARAMETER_LENGTH = 255;

	@Autowired
	private TrialServiceBrapi trialServiceBrapi;

	@Autowired
	private LocationService locationService;

	protected BindingResult errors;

	public BindingResult pruneStudiesInvalidForImport(final List<StudyImportRequestDTO> studyImportRequestDTOS) {
		BaseValidator.checkNotEmpty(studyImportRequestDTOS, "study.import.request.null");
		this.errors = new MapBindingResult(new HashMap<>(), StudyImportRequestDTO.class.getName());

		final List<String> trialDbIds = studyImportRequestDTOS.stream().filter(s -> StringUtils.isNotEmpty(s.getTrialDbId()))
			.map(StudyImportRequestDTO::getTrialDbId).collect(Collectors.toList());
		final TrialSearchRequestDTO trialSearchRequestDTO = new TrialSearchRequestDTO();
		trialSearchRequestDTO.setTrialDbIds(trialDbIds);
		final Map<String, TrialSummary> trialsMap = this.trialServiceBrapi.searchTrials(trialSearchRequestDTO, null).stream()
			.collect(Collectors.toMap(s -> String.valueOf(s.getTrialDbId()), Function.identity()));

		Integer index = 1;
		final Iterator<StudyImportRequestDTO> iterator = studyImportRequestDTOS.iterator();
		while (iterator.hasNext()) {
			final StudyImportRequestDTO s = iterator.next();
			if (StringUtils.isEmpty(s.getTrialDbId())) {
				this.errors.reject("study.import.trialDbId.required", new String[] {index.toString()}, "");
				iterator.remove();
				continue;
			}
			if (!trialsMap.containsKey(s.getTrialDbId())) {
				this.errors.reject("study.import.trialDbId.invalid", new String[] {index.toString()}, "");
				iterator.remove();
				continue;
			}

			if (StringUtils.isNotEmpty(s.getLocationDbId())) {
				final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
				locationSearchRequest.setLocationIds(Collections.singletonList(Integer.valueOf(s.getLocationDbId())));

				if (CollectionUtils.isEmpty(this.locationService.searchLocations(locationSearchRequest, null, null))) {
					this.errors.reject("study.import.locationDbId.invalid", new String[] {index.toString()}, "");
					iterator.remove();
					continue;
				}
			}

			if (!CollectionUtils.isEmpty(s.getSeasons()) && s.getSeasons().size() > 1) {
				this.errors.reject("study.import.season.invalid", new String[] {index.toString()}, "");
				iterator.remove();
				continue;
			}

			if (this.isAnyEnvironmentParametersInvalid(s, index)) {
				iterator.remove();
				continue;
			}

			if (this.isAnyExternalReferenceInvalid(s, index)) {
				iterator.remove();
				continue;
			}
			index++;
		}

		return this.errors;
	}

	private boolean isAnyExternalReferenceInvalid(final StudyImportRequestDTO s, final Integer index) {
		if (s.getExternalReferences() != null) {
			return s.getExternalReferences().stream().anyMatch(r -> {
				if (r == null || StringUtils.isEmpty(r.getReferenceID()) || StringUtils.isEmpty(r.getReferenceSource())) {
					this.errors.reject("study.import.reference.null", new String[] {index.toString(), "externalReference"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceID()) && r.getReferenceID().length() > MAX_REFERENCE_ID_LENGTH) {
					this.errors.reject("study.import.reference.id.exceeded.length", new String[] {index.toString(), "referenceID"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceSource()) && r.getReferenceSource().length() > MAX_REFERENCE_SOURCE_LENGTH) {
					this.errors.reject("study.import.reference.source.exceeded.length", new String[] {index.toString(), "referenceSource"},
						"");
					return true;
				}
				return false;
			});
		}
		return false;
	}

	private boolean isAnyEnvironmentParametersInvalid(final StudyImportRequestDTO s, final Integer index) {
		if (s.getEnvironmentParameters() != null) {
			return s.getEnvironmentParameters().stream().anyMatch(e -> {
				if (StringUtils.isEmpty(e.getParameterPUI())) {
					this.errors.reject("study.import.environment.parameter.pui.null", new String[] {index.toString()}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(e.getValue()) && e.getValue().length() > MAX_ENVIRONMENT_PARAMETER_LENGTH) {
					this.errors.reject("study.import.environment.parameter.value.exceeded.length", new String[] {index.toString()}, "");
					return true;
				}
				return false;
			});
		}
		return false;
	}

}
