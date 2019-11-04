package org.ibp.api.java.impl.middleware.dataset.validator;

import org.apache.commons.lang3.BooleanUtils;
import org.fest.util.Collections;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InstanceValidator {

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyService middlewareStudyService;

	private BindingResult errors;

	public void validate(final Integer datasetId, final Set<Integer> instanceIds) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (!this.studyDataManager.existInstances(instanceIds)) {
			this.errors.reject("dataset.non.existent.instances", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (datasetId != null && !this.studyDataManager.areAllInstancesExistInDataset(datasetId, instanceIds)) {
			this.errors.reject("dataset.invalid.instances", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateForDesignGeneration(final Integer studyId, final Set<Integer> instanceNumbers) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (Collections.isEmpty(instanceNumbers)) {
			this.errors.reject("study.instances.required");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		final Map<String, Integer> instanceGeolocationIdsMap = this.studyDataManager.getInstanceGeolocationIdsMap(studyId);
		final List<String> instanceNumberStringList = instanceNumbers.stream().map(s -> s.toString()).collect(Collectors.toList());
		if (instanceGeolocationIdsMap == null || instanceGeolocationIdsMap.isEmpty() || !instanceGeolocationIdsMap.keySet()
			.containsAll(instanceNumberStringList)) {
			this.errors.reject("dataset.non.existent.instances");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<StudyInstance> studyInstances = this.middlewareStudyService.getStudyInstances(studyId);
		final List<Integer> restrictedInstances = new ArrayList<>();
		final List<Integer> instancesWithDesign = new ArrayList<>();
		for (final StudyInstance instance : studyInstances) {
			if (BooleanUtils.isFalse(instance.isDesignRegenerationAllowed())) {
				restrictedInstances.add(instance.getInstanceNumber());
			}
			if (instance.isHasExperimentalDesign()) {
				instancesWithDesign.add(instance.getInstanceNumber());
			}
		}

		// Check that at least one instance is not restricted from design regeneration
		if (restrictedInstances.containsAll(instanceNumbers)) {
			this.errors.reject("all.selected.instances.cannot.be.regenerated");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		// If at least one instance is for regeneration, check that study has no advanced or crosses list
		if (!instanceNumbers.stream()
			.distinct()
			.filter(instancesWithDesign::contains)
			.collect(Collectors.toSet()).isEmpty() && this.middlewareStudyService.hasAdvancedOrCrossesList(studyId)) {
			this.errors.reject("study.has.advance.or.cross.list");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

}
