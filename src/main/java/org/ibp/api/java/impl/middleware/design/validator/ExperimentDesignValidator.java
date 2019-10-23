package org.ibp.api.java.impl.middleware.design.validator;

import com.google.common.base.Optional;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.HashMap;

@Component
public class ExperimentDesignValidator {

	@Resource
	private org.generationcp.middleware.service.api.study.generation.ExperimentDesignService experimentDesignMiddlewareService;

	private BindingResult errors;

	public void validateExperimentDesignExistence(final Integer studyId, final Boolean experimentDesignShouldExist) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Optional<Integer> experimentDesignTypeTermId = this.experimentDesignMiddlewareService.getStudyExperimentDesignTypeTermId(studyId);

		if (experimentDesignShouldExist && !experimentDesignTypeTermId.isPresent()) {
			this.errors.reject("study.has.no.experiment.design");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!experimentDesignShouldExist && experimentDesignTypeTermId.isPresent()) {
			this.errors.reject("study.already.has.experiment.design");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}



}
