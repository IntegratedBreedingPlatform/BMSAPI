package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.BooleanUtils;
import org.fest.util.Collections;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InstanceValidator {

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyInstanceService studyInstanceService;

	private BindingResult errors;

	public void validate(final Integer datasetId, final Set<Integer> instanceIds) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (Collections.isEmpty(instanceIds)) {
			this.errors.reject("study.instances.required");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!this.studyDataManager.instanceExists(instanceIds)) {
			this.errors.reject("dataset.non.existent.instances", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (datasetId != null && !this.studyDataManager.areAllInstancesExistInDataset(datasetId, instanceIds)) {
			this.errors.reject("dataset.invalid.instances", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateStudyDbId(final int studyDbId) {
		if (!this.studyDataManager.instanceExists(Sets.newHashSet(studyDbId))) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("studydbid.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateInstanceNumbers(final Integer studyId, final Set<Integer> instanceNumbers) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Map<String, Integer> instanceGeolocationIdsMap = this.studyDataManager.getInstanceGeolocationIdsMap(studyId);
		final Set<Integer> selectedInstanceIds =
			instanceGeolocationIdsMap.entrySet().stream().filter(entry -> instanceNumbers.contains(Integer.valueOf(entry.getKey())))
				.map(entry -> entry.getValue()).collect(Collectors.toSet());

		if (instanceNumbers.size() != selectedInstanceIds.size()) {
			this.errors.reject("dataset.non.existent.instances");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateInstancesCanBeDeleted(studyId, selectedInstanceIds, false);
	}

	public void validateStudyInstance(final Integer studyId, final Set<Integer> instanceIds) {
		final Integer environmentDatasetId = this.middlewareStudyService.getEnvironmentDatasetId(studyId);
		this.validate(environmentDatasetId, instanceIds);
	}

	public void validateStudyInstance(final Integer studyId, final Set<Integer> instanceIds, final Boolean studyMustHaveSomeInstances) {
		this.validateStudyInstance(studyId, instanceIds);
		this.validateInstancesCanBeDeleted(studyId, instanceIds, studyMustHaveSomeInstances);
	}

	//FIXME Try to avoid usage of studyMustHaveSomeInstances flag
	private void validateInstancesCanBeDeleted(final Integer studyId, final Set<Integer> instanceIds,
		final Boolean studyMustHaveSomeInstances) {
		final List<StudyInstance> studyInstances = this.studyInstanceService.getStudyInstances(studyId);

		// Raise error if the environment to be deleted is the only remaining environment for study
		if (studyMustHaveSomeInstances && studyInstances.size() < 2) {
			this.errors.reject("cannot.delete.last.instance");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<Integer> restrictedInstances =
			studyInstances.stream().filter(instance -> BooleanUtils.isFalse(instance.getCanBeDeleted()))
				.map(instance -> instance.getInstanceDbId()).collect(Collectors.toList());

		// Raise error if any of the instances are not deletable when enforceAllInstancesDeletable = true
		if (studyMustHaveSomeInstances && !instanceIds.stream()
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
