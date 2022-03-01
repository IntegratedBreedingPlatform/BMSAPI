package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.ontology.OntologyVariableService;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.ibp.api.domain.ontology.AnalysisVariablesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AnalysisVariablesRequestValidator {

	@Autowired
	private OntologyVariableService ontologyVariableService;

	public void validate(final AnalysisVariablesRequest analysisVariablesRequest, final Errors errors) {

		if (StringUtils.isEmpty(analysisVariablesRequest.getVariableType())) {
			errors.reject("analysis.variable.request.variable.type.is.required", "");
		}
		if (CollectionUtils.isEmpty(analysisVariablesRequest.getAnalysisNames())) {
			errors.reject("analysis.variable.request.analysis.names.are.required", "");
		}
		if (CollectionUtils.isEmpty(analysisVariablesRequest.getVariableIds())) {
			errors.reject("analysis.variable.request.variable.ids.are.required", "");
		}
		if (StringUtils.isNotEmpty(analysisVariablesRequest.getVariableType()) && !analysisVariablesRequest.getVariableType()
			.equals(VariableType.ANALYSIS.getName())
			&& !analysisVariablesRequest.getVariableType().equals(VariableType.ANALYSIS_SUMMARY.getName())) {
			errors.reject("analysis.variable.request.invalid.variable.type", "");
		}
		if (CollectionUtils.isNotEmpty(analysisVariablesRequest.getAnalysisNames())) {
			final List<String> list =
				analysisVariablesRequest.getAnalysisNames().stream().map(String::toUpperCase).collect(Collectors.toList());
			final Set<String> duplicateAnalysisNames = list.stream().filter(i -> Collections.frequency(list, i) > 1)
				.collect(Collectors.toSet());
			if (CollectionUtils.isNotEmpty(duplicateAnalysisNames)) {
				errors.reject("analysis.variable.request.duplicate.analysis.names", "");
			}
		}
		this.variablesShouldBeTraitsAndNumerical(analysisVariablesRequest.getVariableIds(), errors);
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
				errors.reject("analysis.variable.request.variables.should.be.numeric.data.type.or.categorical.variable.values.are.numeric", "");
			}

		}
	}

	private boolean filterNonNumericVariableOrNonNumericCategoricalVariables(final Variable variable) {
		if (DataType.NUMERIC_VARIABLE.getName().equals(variable.getScale().getDataType().getName())) {
			return false;
		} else if (DataType.CATEGORICAL_VARIABLE.getName().equals(variable.getScale().getDataType().getName())) {
			return variable.getScale().getCategories().stream().anyMatch(o -> !StringUtils.isNumeric(o.getName()));
		}
		return true;
	}
}
