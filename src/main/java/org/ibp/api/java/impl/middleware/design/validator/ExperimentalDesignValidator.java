package org.ibp.api.java.impl.middleware.design.validator;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.design.ExperimentalDesignService;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ExperimentalDesignValidator {

	@Resource
	private ExperimentalDesignService experimentDesignService;

	private BindingResult errors;

	public void validateExperimentalDesignExistence(final Integer studyId, final Boolean experimentDesignShouldExist) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Optional<Integer> experimentDesignTypeTermId = this.experimentDesignService.getStudyExperimentalDesignTypeTermId(studyId);

		if (experimentDesignShouldExist && !experimentDesignTypeTermId.isPresent()) {
			this.errors.reject("study.has.no.experiment.design");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!experimentDesignShouldExist && experimentDesignTypeTermId.isPresent()) {
			this.errors.reject("study.already.has.experiment.design");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateStudyExperimentalDesign(final Integer studyId, final Integer experimentalDesignTypeId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		// check if design type is valid
		final List<Integer> designTypeIds = this.experimentDesignService.getExperimentalDesignTypes().stream().map(ExperimentDesignType::getId)
			.collect(Collectors.toList());
		if (!designTypeIds.contains(experimentalDesignTypeId)) {
			this.errors.reject("invalid.experimental.design.type");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		// check if specified design type is same as current design (if present) of study
		final Optional<Integer> termIdOptional = this.experimentDesignService.getStudyExperimentalDesignTypeTermId(studyId);
		if (termIdOptional.isPresent()) {
			final ExperimentDesignType experimentDesignType = ExperimentDesignType.getDesignTypeItemByTermId(termIdOptional.get());
			if (!experimentDesignType.getId().equals(experimentalDesignTypeId)) {
				this.errors.reject("design.type.is.different.from.existing.design");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}

	}





}
