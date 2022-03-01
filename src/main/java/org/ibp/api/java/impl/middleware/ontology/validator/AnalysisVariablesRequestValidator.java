package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.ontology.OntologyVariableService;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.ibp.api.domain.ontology.AnalysisVariablesImportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AnalysisVariablesRequestValidator {

	public static final List<String> SUPPORTED_VARIABLE_TYPES =
		ListUtils.unmodifiableList(Arrays.asList(VariableType.ANALYSIS.getName(), VariableType.ANALYSIS_SUMMARY.getName()));

	@Autowired
	private OntologyVariableService ontologyVariableService;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	public void validate(final AnalysisVariablesImportRequest analysisVariablesImportRequest, final Errors errors) {

		if (StringUtils.isEmpty(analysisVariablesImportRequest.getVariableType())) {
			errors.reject("analysis.variable.request.variable.type.is.required", "");
		}
		if (CollectionUtils.isEmpty(analysisVariablesImportRequest.getAnalysisMethodNames())) {
			errors.reject("analysis.variable.request.analysis.method.names.are.required", "");
		}
		if (CollectionUtils.isEmpty(analysisVariablesImportRequest.getVariableIds())) {
			errors.reject("analysis.variable.request.variable.ids.are.required", "");
		}
		if (StringUtils.isNotEmpty(analysisVariablesImportRequest.getVariableType()) && !SUPPORTED_VARIABLE_TYPES.contains(
			analysisVariablesImportRequest.getVariableType())) {
			errors.reject("analysis.variable.request.invalid.variable.type", "");
		}

		if (CollectionUtils.isNotEmpty(analysisVariablesImportRequest.getVariableIds())) {
			final Set<Integer> existingVariableIds =
				this.ontologyDataManager.getTermsByIds(analysisVariablesImportRequest.getVariableIds()).stream().map(Term::getId)
					.collect(Collectors.toSet());
			final Set<Integer> termIdsNotExist =
				analysisVariablesImportRequest.getVariableIds().stream().filter(i -> !existingVariableIds.contains(i))
					.collect(Collectors.toSet());
			if (CollectionUtils.isNotEmpty(termIdsNotExist)) {
				termIdsNotExist.stream().forEach(o -> errors.reject("variable.does.not.exist", new Object[] {o}, ""));
			}
		}

		if (CollectionUtils.isNotEmpty(analysisVariablesImportRequest.getAnalysisMethodNames())) {
			final List<String> list =
				analysisVariablesImportRequest.getAnalysisMethodNames().stream().map(String::toUpperCase).collect(Collectors.toList());
			final Set<String> duplicateAnalysisNames = list.stream().filter(i -> Collections.frequency(list, i) > 1)
				.collect(Collectors.toSet());
			if (CollectionUtils.isNotEmpty(duplicateAnalysisNames)) {
				errors.reject("analysis.variable.request.duplicate.analysis.method.names", "");
			}
		}
		this.variablesShouldBeTraitsAndNumerical(analysisVariablesImportRequest.getVariableIds(), errors);
	}

	private void variablesShouldBeTraitsAndNumerical(final List<Integer> variableIds, final Errors errors) {
		if (CollectionUtils.isNotEmpty(variableIds)) {
			final VariableFilter variableFilter = new VariableFilter();
			variableIds.forEach(variableFilter::addVariableId);
			final Map<Integer, Variable> variableMap = this.ontologyVariableService.getVariablesWithFilterById(variableFilter);
			final Multimap<Integer, VariableType> variableTypesMultimap =
				this.ontologyVariableService.getVariableTypesOfVariables(variableIds);

			final boolean hasNonTraitVariables =
				variableTypesMultimap.asMap().entrySet().stream().anyMatch(e -> !e.getValue().contains(VariableType.TRAIT));
			if (hasNonTraitVariables) {
				errors.reject("analysis.variable.request.variables.should.be.traits", "");
			}

			final List<Variable> nonNumericVariables =
				variableMap.values().stream().filter(this::filterNonNumericVariableOrNonNumericCategoricalVariables)
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(nonNumericVariables)) {
				errors.reject("analysis.variable.request.variables.should.be.numeric.data.type.or.categorical.variable.values.are.numeric",
					"");
			}

		}
	}

	private boolean filterNonNumericVariableOrNonNumericCategoricalVariables(final Variable variable) {
		if (DataType.NUMERIC_VARIABLE.getId().equals(variable.getScale().getDataType().getId())) {
			return false;
		} else if (DataType.CATEGORICAL_VARIABLE.getId().equals(variable.getScale().getDataType().getId())) {
			return variable.getScale().getCategories().stream().anyMatch(o -> !StringUtils.isNumeric(o.getName()));
		}
		return true;
	}
}
