package org.ibp.api.java.impl.middleware.dataset.validator;

import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class InstanceValidator {

	@Autowired
	private StudyDataManager studyDataManager;

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

	public void checkStudyInstanceAlreadyExists(final Integer studyId, final String instanceNumber) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Map<String, Integer> instanceGeolocationIdMap = this.studyDataManager.getInstanceGeolocationIdsMap(studyId);
		if (instanceGeolocationIdMap.keySet().contains(instanceNumber)) {
			this.errors.reject("instance.already.exists", new Object[] {instanceNumber}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
