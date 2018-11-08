package org.ibp.api.java.impl.middleware.dataset.validator;

import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class InstanceValidator {

	@Autowired
	private StudyDataManager studyDataManager;

	private BindingResult errors;

	public void validate(final Integer datasetId, final Integer instanceId) {

		errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (!studyDataManager.isInstanceExistsInDataset(datasetId, instanceId)) {
			errors.reject("instance.does.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
	}

}
