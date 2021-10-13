package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.study.CategoryDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class VariableUpdateValidator {

	private static final String VARIABLE_NAME = "Variable";
	public static final int TERM_NAME_MAX_LENGTH = 200;
	public static final int TERM_DEFINITION_MAX_LENGTH = 1024;
	public static final int CATEROGY_LABEL_MAX_LENGTH = 255;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	protected VariableService variableService;

	@Autowired
	protected TermValidator termValidator;

	public void validate(final String crop, final VariableDTO variableDTO) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.validateVariable(crop, variableDTO, errors);
		this.validateTrait(variableDTO, errors);
		this.validateMethod(variableDTO, errors);
		this.validateScale(variableDTO, errors);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	private void validateVariable(final String crop, final VariableDTO variableDTO, final BindingResult errors) {
		if (StringUtils.isEmpty(variableDTO.getObservationVariableDbId())) {
			errors.reject("observation.variable.update.variable.id.required", new String[] {}, "");
		}
		if (!StringUtils.isNumeric(variableDTO.getObservationVariableDbId())) {
			errors.reject("observation.variable.update.variable.id.should.be.numeric", new String[] {}, "");
		}
		if (StringUtils.isEmpty(variableDTO.getObservationVariableName())) {
			errors.reject("observation.variable.update.variable.name.required", new String[] {}, "");
		}
		if (!StringUtils.isEmpty(variableDTO.getObservationVariableName())
			&& variableDTO.getObservationVariableName().length() > TERM_NAME_MAX_LENGTH) {
			errors.reject("observation.variable.update.variable.name.max.length.exceeded", new String[] {}, "");
		}
		if (!StringUtils.isEmpty(variableDTO.getObservationVariableDbId())) {
			final TermRequest term = new TermRequest(variableDTO.getObservationVariableDbId(), VARIABLE_NAME, CvId.VARIABLES.getId());
			this.termValidator.validate(term, errors);
		}
		if (StringUtils.isNumeric(variableDTO.getObservationVariableDbId())) {
			final boolean isVariableUsedInStudy = this.ontologyVariableDataManager.areVariablesUsedInStudy(
				Arrays.asList(Integer.valueOf(variableDTO.getObservationVariableDbId())));
			if (isVariableUsedInStudy) {
				final VariableDetails variableDetails =
					this.variableService.getVariableById(crop, null, variableDTO.getObservationVariableDbId());
				if (!variableDetails.getProperty().getId().equals(variableDTO.getTrait().getTraitDbId())
					|| !variableDetails.getMethod().getId().equals(variableDTO.getMethod().getMethodDbId())
					|| !variableDetails.getScale().getId().equals(variableDTO.getScale().getScaleDbId())) {
					errors.reject("observation.variable.update.cannot.update.trait.scale.method",
						new String[] {}, "");
				}
			}
		}

	}

	private void validateTrait(final VariableDTO variableDTO, final BindingResult errors) {
		if (variableDTO.getTrait() != null) {
			if (StringUtils.isEmpty(variableDTO.getTrait().getTraitDbId())) {
				errors.reject("observation.variable.update.trait.id.required", new String[] {}, "");
			}
			if (!StringUtils.isNumeric(variableDTO.getTrait().getTraitDbId())) {
				errors.reject("observation.variable.update.trait.id.should.be.numeric", new String[] {}, "");
			}
			if (StringUtils.isEmpty(variableDTO.getTrait().getTraitName())) {
				errors.reject("observation.variable.update.trait.name.required", new String[] {}, "");
			}
			if (!StringUtils.isEmpty(variableDTO.getTrait().getTraitName())
				&& variableDTO.getTrait().getTraitName().length() > TERM_NAME_MAX_LENGTH) {
				errors.reject("observation.variable.update.trait.name.max.length.exceeded", new String[] {}, "");
			}
			if (!StringUtils.isEmpty(variableDTO.getTrait().getTraitDescription())
				&& variableDTO.getTrait().getTraitDescription().length() > TERM_DEFINITION_MAX_LENGTH) {
				errors.reject("observation.variable.update.trait.description.max.length.exceeded", new String[] {}, "");
			}
			if (!StringUtils.isEmpty(variableDTO.getTrait().getTraitDbId())) {
				final TermRequest term = new TermRequest(variableDTO.getTrait().getTraitDbId(), "Property", CvId.PROPERTIES.getId());
				this.termValidator.validate(term, errors);
			}
		}
	}

	private void validateMethod(final VariableDTO variableDTO, final BindingResult errors) {
		if (variableDTO.getMethod() != null) {
			if (StringUtils.isEmpty(variableDTO.getMethod().getMethodDbId())) {
				errors.reject("observation.variable.update.method.id.required", new String[] {}, "");
			}
			if (!StringUtils.isNumeric(variableDTO.getMethod().getMethodDbId())) {
				errors.reject("observation.variable.update.method.id.should.be.numeric", new String[] {}, "");
			}
			if (StringUtils.isEmpty(variableDTO.getMethod().getMethodName())) {
				errors.reject("observation.variable.update.method.name.required", new String[] {}, "");
			}
			if (!StringUtils.isEmpty(variableDTO.getMethod().getMethodName())
				&& variableDTO.getMethod().getMethodName().length() > TERM_NAME_MAX_LENGTH) {
				errors.reject("observation.variable.update.method.name.max.length.exceeded", new String[] {}, "");
			}
			if (!StringUtils.isEmpty(variableDTO.getMethod().getDescription())
				&& variableDTO.getMethod().getDescription().length() > TERM_NAME_MAX_LENGTH) {
				errors.reject("observation.variable.update.method.description.max.length.exceeded", new String[] {}, "");
			}
			if (!StringUtils.isEmpty(variableDTO.getMethod().getMethodDbId())) {
				final TermRequest term = new TermRequest(variableDTO.getMethod().getMethodDbId(), "Method", CvId.METHODS.getId());
				this.termValidator.validate(term, errors);
			}
		}
	}

	private void validateScale(final VariableDTO variableDTO, final BindingResult errors) {
		if (variableDTO.getScale() != null) {
			if (StringUtils.isEmpty(variableDTO.getScale().getScaleDbId())) {
				errors.reject("observation.variable.update.scale.id.required", new String[] {}, "");
			}
			if (!StringUtils.isNumeric(variableDTO.getScale().getScaleDbId())) {
				errors.reject("observation.variable.update.scale.id.should.be.numeric", new String[] {}, "");
			}
			if (StringUtils.isEmpty(variableDTO.getScale().getScaleName())) {
				errors.reject("observation.variable.update.scale.name.required", new String[] {}, "");
			}
			if (!StringUtils.isEmpty(variableDTO.getScale().getScaleName())
				&& variableDTO.getScale().getScaleName().length() > TERM_NAME_MAX_LENGTH) {
				errors.reject("observation.variable.update.scale.name.max.length.exceeded", new String[] {}, "");
			}
			if (!StringUtils.isEmpty(variableDTO.getScale().getScaleDbId())) {
				final TermRequest term = new TermRequest(variableDTO.getScale().getScaleDbId(), "SCALE", CvId.SCALES.getId());
				this.termValidator.validate(term, errors);
			}
			if (variableDTO.getScale().getValidValues() != null && CollectionUtils.isNotEmpty(
				variableDTO.getScale().getValidValues().getCategories())) {
				final List<CategoryDTO> categories = variableDTO.getScale().getValidValues().getCategories();

				if (categories.stream()
					.anyMatch(c -> StringUtils.isNotEmpty(c.getLabel()) && c.getLabel().length() > CATEROGY_LABEL_MAX_LENGTH)) {
					errors.reject("observation.variable.update.scale.categories.label.length.exceeded", new String[] {}, "");
				}
				if (categories.stream()
					.anyMatch(c -> StringUtils.isNotEmpty(c.getValue()) && c.getValue().length() > CATEROGY_LABEL_MAX_LENGTH)) {
					errors.reject("observation.variable.update.scale.categories.value.length.exceeded", new String[] {}, "");
				}

			}
		}
	}

}
