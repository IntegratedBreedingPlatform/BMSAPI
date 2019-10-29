package org.ibp.api.java.impl.middleware.dataset.validator;

import org.apache.commons.lang3.StringUtils;
import org.fest.util.Collections;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyService;
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
	private DatasetService middlewareDatasetService;

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

	public void validate(final Integer studyId, final Set<Integer> instanceNumbers, final Boolean shouldHaveObservations) {
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

		// Check if all specified instances have (or do not have) observations - depending on shouldHaveObservations flag
		final Integer plotDatasetId = this.middlewareStudyService.getPlotDatasetId(studyId);
		final Map<String, Long> observationsCountMap = this.middlewareDatasetService.countObservationsGroupedByInstance(plotDatasetId);
		final List<Integer> instancesWithObservationsAlready = new ArrayList<>();
		final List<Integer> instancesShouldHaveObservations = new ArrayList<>();
		for (final Integer instance : instanceNumbers) {
			final String instanceString = String.valueOf(instance);
			if ((!shouldHaveObservations && observationsCountMap.containsKey(instanceString)
				&& observationsCountMap.get(instanceString) > 0)) {
				instancesWithObservationsAlready.add(instance);

			} else if (shouldHaveObservations && !observationsCountMap.containsKey(instanceString)) {
				instancesShouldHaveObservations.add(instance);
			}
		}

		if (!instancesWithObservationsAlready.isEmpty()) {
			this.errors.reject("instances.already.have.observation", new Object[] {StringUtils.join(instancesWithObservationsAlready, ",")}, null);
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!instancesShouldHaveObservations.isEmpty()) {
			this.errors.reject("instances.should.have.observations", new Object[] {StringUtils.join(instancesShouldHaveObservations, ",")}, null);
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
