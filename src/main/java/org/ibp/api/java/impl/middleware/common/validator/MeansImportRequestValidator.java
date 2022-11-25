package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.impl.analysis.MeansImportRequest;
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
public class MeansImportRequestValidator {

	@Autowired
	private StudyEntryService studyEntryService;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	public void validateEnvironmentNumberIsNotEmpty(final MeansImportRequest meansImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());
		final boolean hasBlankEnvironmentNumbers = meansImportRequest.getData().stream().anyMatch(md -> md.getEnvironmentNumber() == null);
		if (hasBlankEnvironmentNumbers) {
			errors.reject("means.import.means.environment.numbers.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateMeansDataIsNotEmpty(final MeansImportRequest meansImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());
		if (CollectionUtils.isEmpty(meansImportRequest.getData())) {
			errors.reject("means.import.means.data.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateDataValuesIsNotEmpty(final MeansImportRequest meansImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());
		if (meansImportRequest.getData().stream().anyMatch(d -> MapUtils.isEmpty(d.getValues()))) {
			errors.reject("means.import.means.data.values.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateEntryNumberIsNotEmptyAndDistinctPerEnvironment(final MeansImportRequest meansImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());
		final boolean hasBlankEntryNo = meansImportRequest.getData().stream().anyMatch(md -> md.getEntryNo() == null);
		if (hasBlankEntryNo) {
			errors.reject("means.import.means.entry.numbers.required", "");
		} else {
			final Map<Integer, List<MeansImportRequest.MeansData>> meansDataGroupByEnvironmentNumberMap = meansImportRequest.
				getData().stream().collect(Collectors.groupingBy(MeansImportRequest.MeansData::getEnvironmentNumber, Collectors.toList()));
			final boolean hasDuplicateEntryNumbersPerEnvironment =
				meansDataGroupByEnvironmentNumberMap.values().stream().anyMatch(
					meansDataList -> meansDataList.stream()
						.collect(Collectors.groupingBy(MeansImportRequest.MeansData::getEntryNo, Collectors.counting()))
						.entrySet().stream().anyMatch(e -> e.getValue() > 1));
			if (hasDuplicateEntryNumbersPerEnvironment) {
				errors.reject("means.import.means.duplicate.entry.numbers.per.environment", "");
			}
		}
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateAnalysisVariableNames(final MeansImportRequest meansImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());
		if (CollectionUtils.isNotEmpty(meansImportRequest.getData())) {
			final Set<String> analysisVariableNames =
				meansImportRequest.getData().stream()
					.map(o -> !MapUtils.isEmpty(o.getValues()) ? o.getValues().keySet() : new HashSet<String>()).flatMap(Set::stream)
					.collect(Collectors.toSet());

			final VariableFilter variableFilter = new VariableFilter();
			analysisVariableNames.forEach(variableFilter::addName);
			variableFilter.setShowObsoletes(false);
			final Map<String, Variable> variablesMapByName =
				new CaseInsensitiveMap(this.ontologyVariableDataManager.getWithFilter(variableFilter).stream()
					.collect(Collectors.toMap(Variable::getName, Function.identity())));

			final Set<String> nonExistingVariableNames =
				analysisVariableNames.stream().filter(o -> !variablesMapByName.containsKey(o)).collect(Collectors.toSet());

			if (CollectionUtils.isNotEmpty(nonExistingVariableNames)) {
				errors.reject("means.import.means.analysis.variable.names.do.not.exist",
					new Object[] {StringUtils.join(nonExistingVariableNames, ", ")}, "");
			}

			final List<Variable> invalidVariables =
				variablesMapByName.values().stream().filter(o -> !o.getVariableTypes().contains(VariableType.ANALYSIS))
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(invalidVariables)) {
				errors.reject("means.import.means.variables.must.be.analysis.type",
					new Object[] {StringUtils.join(invalidVariables.stream().map(Variable::getName).collect(Collectors.toList()), ", ")},
					"");
			}
		}
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

}
