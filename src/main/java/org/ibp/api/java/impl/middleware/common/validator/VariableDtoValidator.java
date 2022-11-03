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
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class VariableDtoValidator {

	public static final int TERM_NAME_MAX_LENGTH = 32;
	final Pattern variableNameValidPattern = Pattern.compile("[$&+,./%')\\[}\\]{(\\\\\\-*^!`~:;=?@#|\\s]");

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private VariableServiceBrapi variableServiceBrapi;

	@Autowired
	private TermValidator termValidator;

	@Autowired
	private StudyServiceBrapi studyServiceBrapi;

	public void validateForUpdate(final String observationVariableDbId, final VariableDTO variableDTO) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		this.validateObservationVariableDbId(observationVariableDbId, variableDTO, errors);
		this.validateObservationVariableName(variableDTO, errors);
		this.validateTrait(variableDTO, errors);
		this.validateMethod(variableDTO, errors);
		this.validateScale(variableDTO, errors);
		this.checkVariableIsUsedInStudy(variableDTO, errors);
		this.validateStudyDbIds(variableDTO, errors);
		this.validateContextOfUse(variableDTO, errors);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	public BindingResult pruneVariablesInvalidForImport(final List<VariableDTO> variableDTOList) {
		final BindingResult allErrors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
		final Iterator<VariableDTO> variableDTOIterator = variableDTOList.iterator();
		final List<String> existingVariableNames = this.getExistingVariableNames(variableDTOList);
		while (variableDTOIterator.hasNext()) {
			final VariableDTO variableDTO = variableDTOIterator.next();
			final BindingResult errors = new MapBindingResult(new HashMap<>(), VariableDTO.class.getName());
			this.validateObservationVariableName(variableDTO, errors);
			this.validateTrait(variableDTO, errors);
			this.validateMethod(variableDTO, errors);
			this.validateScale(variableDTO, errors);
			this.checkDuplicatePropertyScaleMethodCombination(variableDTO, errors);
			this.validateContextOfUse(variableDTO, errors);
			this.validateExistingObservationVariableName(variableDTO, existingVariableNames, errors);
			if (errors.hasErrors()) {
				allErrors.addAllErrors(errors);
				variableDTOIterator.remove();
			}
		}
		return allErrors;
	}

	protected void validateObservationVariableDbId(final String observationVariableDbId, final VariableDTO variableDTO,
		final BindingResult errors) {
		if (StringUtils.isEmpty(variableDTO.getObservationVariableDbId())) {
			errors.reject("observation.variable.variable.id.required", new String[] {}, "");
			return;
		}
		if (!StringUtils.isNumeric(variableDTO.getObservationVariableDbId())) {
			errors.reject("observation.variable.variable.id.should.be.numeric", new String[] {}, "");
			return;
		}
		if (observationVariableDbId != null && !observationVariableDbId.equalsIgnoreCase(variableDTO.getObservationVariableDbId())) {
			errors.reject("observation.variable.variable.id.and.path.variable.id.not.equal", new String[] {}, "");
			return;
		}
		final VariableSearchRequestDTO variableSearchRequestDTO = new VariableSearchRequestDTO();
		variableSearchRequestDTO.setFilterObsoletes(true);
		variableSearchRequestDTO.setObservationVariableDbIds(Collections.singletonList(variableDTO.getObservationVariableDbId()));
		final List<VariableDTO> variableDTOS =
			this.variableServiceBrapi.getVariables(variableSearchRequestDTO, null, VariableTypeGroup.TRAIT);
		if (CollectionUtils.isEmpty(variableDTOS)) {
			errors.reject("observation.variable.variable.id.invalid", new String[] {}, "");
		}
	}

	protected void validateObservationVariableName(final VariableDTO variableDTO, final BindingResult errors) {
		if (StringUtils.isEmpty(variableDTO.getObservationVariableName())) {
			errors.reject("observation.variable.variable.name.required", new String[] {}, "");
			return;
		}
		if (!StringUtils.isEmpty(variableDTO.getObservationVariableName())
			&& variableDTO.getObservationVariableName().length() > TERM_NAME_MAX_LENGTH) {
			errors.reject("observation.variable.variable.name.max.length.exceeded", new String[] {}, "");
		}
		final Matcher matcher = this.variableNameValidPattern.matcher(variableDTO.getObservationVariableName());
		if (matcher.find() || Character.isDigit(variableDTO.getObservationVariableName().charAt(0))) {
			errors.reject("observation.variable.variable.name.should.have.valid.pattern", new String[] {}, "");
		}
	}

	protected void validateTrait(final VariableDTO variableDTO, final BindingResult errors) {
		if (variableDTO.getTrait() != null) {
			if (StringUtils.isEmpty(variableDTO.getTrait().getTraitDbId())) {
				errors.reject("observation.variable.trait.id.required", new String[] {}, "");
				return;
			}
			if (!StringUtils.isNumeric(variableDTO.getTrait().getTraitDbId())) {
				errors.reject("observation.variable.trait.id.should.be.numeric", new String[] {}, "");
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
				errors.reject("observation.variable.method.id.required", new String[] {}, "");
				return;
			}
			if (!StringUtils.isNumeric(variableDTO.getMethod().getMethodDbId())) {
				errors.reject("observation.variable.method.id.should.be.numeric", new String[] {}, "");
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
				errors.reject("observation.variable.scale.id.required", new String[] {}, "");
				return;
			}
			if (!StringUtils.isNumeric(variableDTO.getScale().getScaleDbId())) {
				errors.reject("observation.variable.scale.id.should.be.numeric", new String[] {}, "");
				return;
			}
			if (!StringUtils.isEmpty(variableDTO.getScale().getScaleDbId())) {
				final TermRequest term = new TermRequest(variableDTO.getScale().getScaleDbId(), "SCALE", CvId.SCALES.getId());
				this.termValidator.validate(term, errors);
			}
		}
	}

	protected void checkVariableIsUsedInStudy(final VariableDTO variableDTO, final BindingResult errors) {
		if (StringUtils.isNumeric(variableDTO.getObservationVariableDbId())) {
			final boolean isVariableUsedInStudy = this.ontologyVariableDataManager.areVariablesUsedInStudy(
				Arrays.asList(Integer.valueOf(variableDTO.getObservationVariableDbId())));
			if (isVariableUsedInStudy) {
				final Variable variableDetails = this.ontologyVariableDataManager.getVariable(StringUtils.EMPTY,
					Integer.valueOf(variableDTO.getObservationVariableDbId()), true);
				if (variableDTO.getTrait() != null && StringUtils.isNotEmpty(variableDTO.getTrait().getTraitDbId())
					&& variableDTO.getScale() != null && StringUtils.isNotEmpty(variableDTO.getScale().getScaleDbId())
					&& variableDTO.getMethod() != null && StringUtils.isNotEmpty(variableDTO.getMethod().getMethodDbId())
					&& (variableDetails.getProperty().getId() != Integer.parseInt(variableDTO.getTrait().getTraitDbId())
					|| variableDetails.getMethod().getId() != Integer.parseInt(variableDTO.getMethod().getMethodDbId())
					|| variableDetails.getScale().getId() != Integer.parseInt(variableDTO.getScale().getScaleDbId()))) {
					errors.reject("observation.variable.cannot.update.trait.scale.method",
						new String[] {}, "");
				}
			}
		}
	}

	protected void validateStudyDbIds(final VariableDTO variableDTO, final BindingResult errors) {
		if (!CollectionUtils.isEmpty(variableDTO.getStudyDbIds())) {
			final StudySearchFilter studySearchFilter = new StudySearchFilter();
			studySearchFilter.setStudyDbIds(variableDTO.getStudyDbIds());
			final List<String> existingStudyDbIds =
				this.studyServiceBrapi.getStudyInstances(studySearchFilter, null).stream()
					.map(StudyInstanceDto::getStudyDbId).collect(Collectors.toList());
			final List<String> invalidStudyDbIds =
				new ArrayList<>(CollectionUtils.subtract(variableDTO.getStudyDbIds(), existingStudyDbIds));

			if (CollectionUtils.isNotEmpty(invalidStudyDbIds)) {
				final String invalidStudyIdsString = invalidStudyDbIds.stream()
					.collect(Collectors.joining(", "));
				errors.reject("observation.variable.study.id.invalid", new String[] {invalidStudyIdsString}, "");
			}
		}
	}

	protected void checkDuplicatePropertyScaleMethodCombination(final VariableDTO variableDTO, final BindingResult errors) {

		if (StringUtils.isBlank(variableDTO.getMethod().getMethodDbId()) ||
			!StringUtils.isNumeric(variableDTO.getMethod().getMethodDbId()) ||
			StringUtils.isBlank(variableDTO.getTrait().getTraitDbId()) ||
			!StringUtils.isNumeric(variableDTO.getTrait().getTraitDbId()) ||
			StringUtils.isBlank(variableDTO.getScale().getScaleDbId()) ||
			!StringUtils.isNumeric(variableDTO.getScale().getScaleDbId())) {
			return;
		}

		final Integer methodId = StringUtil.parseInt(variableDTO.getMethod().getMethodDbId(), null);
		final Integer propertyId = StringUtil.parseInt(variableDTO.getTrait().getTraitDbId(), null);
		final Integer scaleId = StringUtil.parseInt(variableDTO.getScale().getScaleDbId(), null);

		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addMethodId(methodId);
		variableFilter.addPropertyId(propertyId);
		variableFilter.addScaleId(scaleId);

		final List<Variable> variableSummary = this.ontologyVariableDataManager.getWithFilter(variableFilter);

		if (variableSummary.size() > 1 || variableSummary.size() == 1
			&& !Objects.equals(String.valueOf(variableSummary.get(0).getId()), variableDTO.getObservationVariableDbId())) {
			errors.reject("observation.variable.trait.scale.method.combination.already.exists",
				new String[] {String.valueOf(propertyId), String.valueOf(scaleId), String.valueOf(methodId)}, "");
		}
	}

	protected void validateContextOfUse(final VariableDTO variableDTO, final BindingResult errors) {
		final List<String> validContextOfUse =
			Arrays.stream(VariableDTO.ContextOfUseEnum.values()).map(VariableDTO.ContextOfUseEnum::toString).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(variableDTO.getContextOfUse()) && variableDTO.getContextOfUse().stream()
			.anyMatch(s -> !validContextOfUse.contains(s))) {
			errors.reject("observation.variable.invalid.context.of.use",
				new Object[] {String.join(", ", validContextOfUse)}, "");
		}
		if (variableDTO.getContextOfUse().containsAll(validContextOfUse)) {
			errors.reject("observation.variable.context.of.use.specify.only.one.valid.value",
				new Object[] {String.join(", ", validContextOfUse)}, "");
		}
	}

	protected List<String> getExistingVariableNames(final List<VariableDTO> variableDTOList) {
		final List<String> observationVariableNames =
			variableDTOList.stream().filter(o -> StringUtils.isNotEmpty(o.getObservationVariableName()))
				.map(VariableDTO::getObservationVariableName).map(String::toUpperCase).collect(
					Collectors.toList());
		final VariableFilter variableFilter = new VariableFilter();
		observationVariableNames.stream().forEach(variableFilter::addName);
		return this.ontologyVariableDataManager.getWithFilter(variableFilter).stream().map(Variable::getName).map(String::toUpperCase)
			.collect(
				Collectors.toList());
	}

	protected void validateExistingObservationVariableName(final VariableDTO variableDTO,
		final List<String> existingObservationVariableNames,
		final BindingResult errors) {
		if (existingObservationVariableNames.stream()
			.anyMatch(variableName -> variableName.equalsIgnoreCase(variableDTO.getObservationVariableName()))) {
			errors.reject("observation.variable.variable.name.already.exist",
				new Object[] {variableDTO.getObservationVariableName()}, "");
		}
	}

}
