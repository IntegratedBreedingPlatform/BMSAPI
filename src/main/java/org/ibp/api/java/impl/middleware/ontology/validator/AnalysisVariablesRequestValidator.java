package org.ibp.api.java.impl.middleware.ontology.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.ibp.api.domain.ontology.AnalysisVariablesRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AnalysisVariablesRequestValidator {

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
	}
}
