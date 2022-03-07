package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.StudyServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableServiceBrapi;
import org.generationcp.middleware.api.brapi.VariableTypeGroup;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class VariableUpdateValidator {

	public static final int TERM_NAME_MAX_LENGTH = 200;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private VariableServiceBrapi variableServiceBrapi;

	@Autowired
	private TermValidator termValidator;

	@Autowired
	private StudyServiceBrapi studyServiceBrapi;

	public void validate(final String observationVariableDbId, final VariableDTO variableDTO) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.validateVariable(observationVariableDbId, variableDTO, errors);
		this.validateTrait(variableDTO, errors);
		this.validateMethod(variableDTO, errors);
		this.validateScale(variableDTO, errors);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	protected void validateVariable(final String observationVariableDbId, final VariableDTO variableDTO, final BindingResult errors) {
		if (StringUtils.isEmpty(variableDTO.getObservationVariableDbId())) {
			errors.reject("observation.variable.update.variable.id.required", new String[] {}, "");
			return;
		}
		if(!observationVariableDbId.equalsIgnoreCase(variableDTO.getObservationVariableDbId())) {
			errors.reject("observation.variable.update.variable.id.and.path.variable.id.not.equal", new String[] {}, "");
			return;
		}
		if (!StringUtils.isNumeric(variableDTO.getObservationVariableDbId())) {
			errors.reject("observation.variable.update.variable.id.should.be.numeric", new String[] {}, "");
			return;
		}
		final VariableSearchRequestDTO variableSearchRequestDTO = new VariableSearchRequestDTO();
		variableSearchRequestDTO.setObservationVariableDbIds(Collections.singletonList(variableDTO.getObservationVariableDbId()));
		final List<VariableDTO> variableDTOS =
			this.variableServiceBrapi.getVariables(variableSearchRequestDTO, null, VariableTypeGroup.TRAIT);
		if (CollectionUtils.isEmpty(variableDTOS)) {
			errors.reject("observation.variable.update.variable.id.invalid", new String[] {}, "");
			return;
		}
		if (StringUtils.isEmpty(variableDTO.getObservationVariableName())) {
			errors.reject("observation.variable.update.variable.name.required", new String[] {}, "");
			return;
		}
		if (!StringUtils.isEmpty(variableDTO.getObservationVariableName())
			&& variableDTO.getObservationVariableName().length() > TERM_NAME_MAX_LENGTH) {
			errors.reject("observation.variable.update.variable.name.max.length.exceeded", new String[] {}, "");
			return;
		}
		if (StringUtils.isNumeric(variableDTO.getObservationVariableDbId())) {
			final boolean isVariableUsedInStudy = this.ontologyVariableDataManager.areVariablesUsedInStudy(
				Arrays.asList(Integer.valueOf(variableDTO.getObservationVariableDbId())));
			if (isVariableUsedInStudy) {
				final Variable variableDetails = this.ontologyVariableDataManager.getVariable(StringUtils.EMPTY, Integer.valueOf(variableDTO.getObservationVariableDbId()), true);
				if (variableDTO.getTrait() != null && StringUtils.isNotEmpty(variableDTO.getTrait().getTraitDbId())
						&& variableDTO.getScale() != null && StringUtils.isNotEmpty(variableDTO.getScale().getScaleDbId())
						&& variableDTO.getMethod() != null && StringUtils.isNotEmpty(variableDTO.getMethod().getMethodDbId())
						&& (variableDetails.getProperty().getId() != Integer.parseInt(variableDTO.getTrait().getTraitDbId())
					|| variableDetails.getMethod().getId() != Integer.parseInt(variableDTO.getMethod().getMethodDbId())
					|| variableDetails.getScale().getId() != Integer.parseInt(variableDTO.getScale().getScaleDbId()))) {
					errors.reject("observation.variable.update.cannot.update.trait.scale.method",
						new String[] {}, "");
					return;
				}
			}
		}
		if (!CollectionUtils.isEmpty(variableDTO.getStudyDbIds())) {
			final StudySearchFilter studySearchFilter = new StudySearchFilter();
			studySearchFilter.setStudyDbIds(variableDTO.getStudyDbIds());
			final List<String> existingStudyDbIds =
					this.studyServiceBrapi.getStudyInstances(studySearchFilter, null).stream()
							.map(StudyInstanceDto::getStudyDbId).collect(Collectors.toList());
			final List<String> invalidStudyDbIds = new ArrayList<>(CollectionUtils.subtract(variableDTO.getStudyDbIds(), existingStudyDbIds));

			if(CollectionUtils.isNotEmpty(invalidStudyDbIds)) {
				final String invalidStudyIdsString = invalidStudyDbIds.stream()
						.collect(Collectors.joining(", "));
				errors.reject("observation.variable.update.study.id.invalid", new String[] {invalidStudyIdsString}, "");
			}
		}

	}

	protected void validateTrait(final VariableDTO variableDTO, final BindingResult errors) {
		if (variableDTO.getTrait() != null) {
			if (StringUtils.isEmpty(variableDTO.getTrait().getTraitDbId())) {
				errors.reject("observation.variable.update.trait.id.required", new String[] {}, "");
				return;
			}
			if (!StringUtils.isNumeric(variableDTO.getTrait().getTraitDbId())) {
				errors.reject("observation.variable.update.trait.id.should.be.numeric", new String[] {}, "");
				return;
			}
			if (!StringUtils.isEmpty(variableDTO.getTrait().getTraitDbId())) {
				final TermRequest term = new TermRequest(variableDTO.getTrait().getTraitDbId(), "Property", CvId.PROPERTIES.getId());
				this.termValidator.validate(term, errors);
			}
		}
	}

	protected void validateMethod(final VariableDTO variableDTO, final BindingResult errors) {
		if (variableDTO.getMethod() != null) {
			if (StringUtils.isEmpty(variableDTO.getMethod().getMethodDbId())) {
				errors.reject("observation.variable.update.method.id.required", new String[] {}, "");
				return;
			}
			if (!StringUtils.isNumeric(variableDTO.getMethod().getMethodDbId())) {
				errors.reject("observation.variable.update.method.id.should.be.numeric", new String[] {}, "");
				return;
			}
			if (!StringUtils.isEmpty(variableDTO.getMethod().getMethodDbId())) {
				final TermRequest term = new TermRequest(variableDTO.getMethod().getMethodDbId(), "Method", CvId.METHODS.getId());
				this.termValidator.validate(term, errors);
			}
		}
	}

	protected void validateScale(final VariableDTO variableDTO, final BindingResult errors) {
		if (variableDTO.getScale() != null) {
			if (StringUtils.isEmpty(variableDTO.getScale().getScaleDbId())) {
				errors.reject("observation.variable.update.scale.id.required", new String[] {}, "");
				return;
			}
			if (!StringUtils.isNumeric(variableDTO.getScale().getScaleDbId())) {
				errors.reject("observation.variable.update.scale.id.should.be.numeric", new String[] {}, "");
				return;
			}
			if (!StringUtils.isEmpty(variableDTO.getScale().getScaleDbId())) {
				final TermRequest term = new TermRequest(variableDTO.getScale().getScaleDbId(), "SCALE", CvId.SCALES.getId());
				this.termValidator.validate(term, errors);
			}
		}
	}

}
