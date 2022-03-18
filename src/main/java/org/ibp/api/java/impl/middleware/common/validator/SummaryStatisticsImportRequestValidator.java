package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.service.impl.analysis.SummaryStatisticsImportRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.thymeleaf.util.MapUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SummaryStatisticsImportRequestValidator {

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	public void validateEnvironmentNumberIsNotEmpty(final SummaryStatisticsImportRequest summaryStatisticsImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), SummaryStatisticsImportRequest.class.getName());
		final boolean hasBlankEnvironmentNumbers =
			summaryStatisticsImportRequest.getData().stream().anyMatch(md -> md.getEnvironmentNumber() == null);
		if (hasBlankEnvironmentNumbers) {
			errors.reject("summary.statistics.import.environment.numbers.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateSummaryDataIsNotEmpty(final SummaryStatisticsImportRequest summaryStatisticsImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), SummaryStatisticsImportRequest.class.getName());
		if (CollectionUtils.isEmpty(summaryStatisticsImportRequest.getData())) {
			errors.reject("summary.statistics.import.data.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateDataValuesIsNotEmpty(final SummaryStatisticsImportRequest summaryStatisticsImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), SummaryStatisticsImportRequest.class.getName());
		if (summaryStatisticsImportRequest.getData().stream().anyMatch(d -> MapUtils.isEmpty(d.getValues()))) {
			errors.reject("summary.statistics.import.data.values.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateAnalysisVariableNames(final SummaryStatisticsImportRequest summaryStatisticsImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), SummaryStatisticsImportRequest.class.getName());
		if (CollectionUtils.isNotEmpty(summaryStatisticsImportRequest.getData())) {
			final Set<String> analysisSummaryVariableNames =
				summaryStatisticsImportRequest.getData().stream()
					.map(o -> !MapUtils.isEmpty(o.getValues()) ? o.getValues().keySet() : new HashSet<String>()).flatMap(Set::stream)
					.collect(Collectors.toSet());

			final VariableFilter variableFilter = new VariableFilter();
			analysisSummaryVariableNames.forEach(variableFilter::addName);
			final Map<String, Variable> variablesMapByName =
				new CaseInsensitiveMap(this.ontologyVariableDataManager.getWithFilter(variableFilter).stream()
					.collect(Collectors.toMap(Variable::getName, Function.identity())));

			final Set<String> nonExistingVariableNames =
				analysisSummaryVariableNames.stream().filter(o -> !variablesMapByName.containsKey(o)).collect(Collectors.toSet());

			if (CollectionUtils.isNotEmpty(nonExistingVariableNames)) {
				errors.reject("summary.statistics.analysis.summary.variable.names.do.not.exist",
					new Object[] {StringUtils.join(nonExistingVariableNames, ", ")}, "");
			}

			final List<Variable> invalidVariables =
				variablesMapByName.values().stream().filter(o -> !o.getVariableTypes().contains(VariableType.ANALYSIS_SUMMARY))
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(invalidVariables)) {
				errors.reject("summary.statistics.import.variables.must.be.analysis.summary.type",
					new Object[] {StringUtils.join(invalidVariables.stream().map(Variable::getName).collect(Collectors.toList()), ", ")},
					"");
			}
		}
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateEnvironmentNumberIsDistinct(final SummaryStatisticsImportRequest summaryStatisticsImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), SummaryStatisticsImportRequest.class.getName());
		final boolean hasDuplicateEnvironmentNumbers =
			summaryStatisticsImportRequest.getData().stream()
				.collect(Collectors.groupingBy(SummaryStatisticsImportRequest.SummaryData::getEnvironmentNumber, Collectors.counting()))
				.entrySet().stream().anyMatch(e -> e.getValue() > 1);
		if (hasDuplicateEnvironmentNumbers) {
			errors.reject("summary.statistics.import.duplicate.environment.number", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}
}
