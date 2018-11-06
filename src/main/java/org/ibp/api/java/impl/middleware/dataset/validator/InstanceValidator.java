package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.base.Optional;
import org.generationcp.middleware.dao.dms.InstanceMetadata;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.exception.ApiRequestValidationException;
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

	public void validate(final Integer studyId, final Integer instanceId) {

		errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Optional<InstanceMetadata> instanceMetadataOptional = studyDataManager.getInstanceMetadataByInstanceId(studyId, instanceId);

		if (!instanceMetadataOptional.isPresent()) {
			errors.reject("instance.does.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
	}

}
