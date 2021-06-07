package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class StudyImportRequestValidator {

	@Autowired
	private StudyService studyService;

	@Autowired
	private LocationService locationService;

	protected BindingResult errors;

	public BindingResult pruneStudiesInvalidForImport(final List<StudyImportRequestDTO> studyImportRequestDTOS, final String crop) {
		BaseValidator.checkNotEmpty(studyImportRequestDTOS, "study.import.request.null");
		this.errors = new MapBindingResult(new HashMap<>(), StudyImportRequestDTO.class.getName());
		final Map<StudyImportRequestDTO, Integer> importRequestByIndexMap = IntStream.range(0, studyImportRequestDTOS.size())
			.boxed().collect(Collectors.toMap(studyImportRequestDTOS::get, i -> i));

		studyImportRequestDTOS.removeIf(s -> {
			final Integer index = importRequestByIndexMap.get(s) + 1;

			if (StringUtils.isEmpty(s.getTrialDbId())) {
				this.errors.reject("study.import.trialDbId.required", new String[] {index.toString()}, "");
				return true;
			}

			final StudySearchFilter filter = new StudySearchFilter();
			filter.setTrialDbIds(Collections.singletonList(s.getTrialDbId()));
			final List<StudySummary> studies = this.studyService.getStudies(filter, null);
			if (CollectionUtils.isEmpty(studies)) {
				this.errors.reject("study.import.trialDbId.invalid", new String[] {index.toString()}, "");
				return true;
			}

			if (StringUtils.isNotEmpty(s.getLocationDbId())) {
				final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
				locationSearchRequest.setLocationIds(Collections.singletonList(Integer.valueOf(s.getLocationDbId())));
				//Get the progamUUID from the Study where the environment will be added
				locationSearchRequest.setProgramUUID(studies.get(0).getProgramDbId());

				if (CollectionUtils.isEmpty(this.locationService.getFilteredLocations(locationSearchRequest, null))) {
					this.errors.reject("study.import.locationDbId.invalid", new String[] {index.toString()}, "");
					return true;
				}
			}

			if (this.isAnyEnvironmentParametersInvalid(s, index)) {
				return true;
			}

			if (this.isAnyExternalReferenceInvalid(s, index)) {
				return true;
			}

			return false;
		});

		return this.errors;
	}

	private boolean isAnyExternalReferenceInvalid(final StudyImportRequestDTO s, final Integer index) {
		if (s.getExternalReferences() != null) {
			return s.getExternalReferences().stream().anyMatch(r -> {
				if (r == null || StringUtils.isEmpty(r.getReferenceID()) || StringUtils.isEmpty(r.getReferenceSource())) {
					this.errors.reject("study.import.reference.null", new String[] {index.toString(), "externalReference"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceID()) && r.getReferenceID().length() > 2000) {
					this.errors.reject("study.import.reference.id.exceeded.length", new String[] {index.toString(), "referenceID"}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceSource()) && r.getReferenceSource().length() > 255) {
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
				if (StringUtils.isNotEmpty(e.getValue()) && e.getValue().length() > 255) {
					this.errors.reject("study.import.environment.parameter.value.exceeded.length", new String[] {index.toString()}, "");
					return true;
				}
				return false;
			});
		}
		return false;
	}

}
