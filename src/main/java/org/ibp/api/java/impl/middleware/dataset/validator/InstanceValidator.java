package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.BooleanUtils;
import org.fest.util.Collections;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.StudyEnvironmentService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InstanceValidator {

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyEnvironmentService studyEnvironmentService;

	private BindingResult errors;

	public void validate(final Integer datasetId, final Set<Integer> instanceIds) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (Collections.isEmpty(instanceIds)) {
			this.errors.reject("study.instances.required");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!this.studyDataManager.areAllInstancesExistInDataset(datasetId, instanceIds)) {
			this.errors.reject("dataset.non.existent.instances", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (datasetId != null && !this.studyDataManager.areAllInstancesExistInDataset(datasetId, instanceIds)) {
			this.errors.reject("dataset.invalid.instances", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateStudyDbId(final int studyDbId) {
		if (!this.studyDataManager.instancesExist(Sets.newHashSet(studyDbId))) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("studydbid.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateInstanceNumbers(final Integer studyId, final Set<Integer> instanceNumbers) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final List<StudyInstance> studyInstances = this.studyEnvironmentService.getStudyEnvironments(studyId);
		final Set<Integer> selectedInstanceIds =
			studyInstances.stream().filter(instance -> instanceNumbers.contains(instance.getInstanceNumber()))
				.map(StudyInstance::getExperimentId).collect(Collectors.toSet());

		if (instanceNumbers.size() != selectedInstanceIds.size()) {
			this.errors.reject("dataset.non.existent.instances");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateInstancesDeletability(studyId, selectedInstanceIds, false);
	}

	public void validateStudyInstance(final Integer studyId, final Set<Integer> instanceIds) {
		final Integer environmentDatasetId = this.middlewareStudyService.getEnvironmentDatasetId(studyId);
		this.validate(environmentDatasetId, instanceIds);
	}

	public void validateStudyInstance(final Integer studyId, final Set<Integer> instanceIds, final Boolean enforceAllInstancesDeletable) {
		this.validateStudyInstance(studyId, instanceIds);
		this.validateInstancesDeletability(studyId, instanceIds, enforceAllInstancesDeletable);
	}

	private void validateInstancesDeletability(final Integer studyId, final Set<Integer> instanceIds,
		final Boolean enforceAllInstancesDeletable) {
		final List<StudyInstance> studyInstances = this.studyEnvironmentService.getStudyEnvironments(studyId);

		// Raise error if the environment/s to be deleted will cause study to have no remaining environment
		if (enforceAllInstancesDeletable && (studyInstances.size() - instanceIds.size()) < 1) {
			this.errors.reject("cannot.delete.last.instance");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<Integer> restrictedInstances =
			studyInstances.stream().filter(instance -> BooleanUtils.isFalse(instance.getCanBeDeleted()))
				.map(StudyInstance::getExperimentId).collect(Collectors.toList());

		// Raise error if any of the instances are not deletable when enforceAllInstancesDeletable = true
		if (enforceAllInstancesDeletable && !instanceIds.stream()
			.distinct()
			.filter(restrictedInstances::contains)
			.collect(Collectors.toSet()).isEmpty()) {
			this.errors.reject("at.least.one.instance.cannot.be.deleted");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		// Verify at least one instance can be re/generated or deleted
		if (restrictedInstances.containsAll(instanceIds)) {
			this.errors.reject("all.selected.instances.cannot.be.regenerated");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
