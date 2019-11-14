package org.ibp.api.java.impl.middleware.dataset.validator;

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

		if (!this.studyDataManager.existInstances(instanceIds)) {
			this.errors.reject("dataset.non.existent.instances", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (datasetId != null && !this.studyDataManager.areAllInstancesExistInDataset(datasetId, instanceIds)) {
			this.errors.reject("dataset.invalid.instances", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateInstanceNumbers(final Integer studyId, final Set<Integer> instanceNumbers, final Boolean enforceAllInstancesDeletable) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Map<String, Integer> instanceGeolocationIdsMap = this.studyDataManager.getInstanceGeolocationIdsMap(studyId);
		final List<Integer> selectedInstanceIds =
			instanceGeolocationIdsMap.entrySet().stream().filter(entry -> instanceNumbers.contains(Integer.valueOf(entry.getKey())))
				.map(entry -> entry.getValue()).collect(Collectors.toList());

		if (instanceNumbers.size() != selectedInstanceIds.size()) {
			this.errors.reject("dataset.non.existent.instances");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateInstancesDeletability(studyId, instanceNumbers, enforceAllInstancesDeletable);
	}

	public void validateInstanceDeletion(final Integer studyId, final Set<Integer> instanceIds, final Boolean enforceAllInstancesDeletable) {
		this.validate(studyId, instanceIds);

		this.validateInstancesDeletability(studyId, instanceIds, enforceAllInstancesDeletable);

	}

	private void validateInstancesDeletability(final Integer studyId, final Set<Integer> instanceIds,
		final Boolean enforceAllInstancesDeletable) {
		final List<StudyInstance> studyInstances = this.studyInstanceService.getStudyInstances(studyId);
		final List<Integer> restrictedInstances =
			studyInstances.stream().filter(instance -> BooleanUtils.isFalse(instance.getCanBeDeleted()))
				.map(instance -> instance.getInstanceDbId()).collect(Collectors.toList());

		// Raise error if any of the instances are not deletable when enforceAllInstancesDeletable = true
		if (enforceAllInstancesDeletable && !instanceIds.stream()
			.distinct()
			.filter(restrictedInstances::contains)
			.collect(Collectors.toSet()).isEmpty()) {
			this.errors.reject("at.least.one.instance.cannot.be.deleted");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		// Verify at least one instance can be re/generated
		if (restrictedInstances.containsAll(instanceIds)) {
			this.errors.reject("all.selected.instances.cannot.be.regenerated");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
