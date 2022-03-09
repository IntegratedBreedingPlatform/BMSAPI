package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
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

import java.util.ArrayList;
import java.util.Collection;
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
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyEntryService studyEntryService;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	public void validate(final MeansImportRequest meansImportRequest) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());

		// Validate study
		this.validateStudy(meansImportRequest);
		// Check if means dataset already exists
		this.checkIfMeansDatasetAlreadyExists(meansImportRequest);
		// Validate means data
		this.checkMeansDataIsEmpty(meansImportRequest);
		this.checkDataValuesIsEmpty(meansImportRequest);
		// Validate environmentNumber
		this.validateEnvironmentNumbers(meansImportRequest);
		// Validate entryNumber
		this.validateEntryNumber(meansImportRequest, errors);
		// Validate analysis variable names
		this.validateAnalysisVariableNames(meansImportRequest, errors);

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	protected void validateStudy(final MeansImportRequest meansImportRequest) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());

		if (meansImportRequest.getStudyId() == null) {
			errors.reject("study.required", "");
		} else {
			final Study study = this.studyDataManager.getStudy(meansImportRequest.getStudyId());
			if (study == null || study.getType() == null) {
				errors.reject("study.not.exist", "");
			}
		}
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	protected void checkIfMeansDatasetAlreadyExists(final MeansImportRequest meansImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());
		if (meansImportRequest.getStudyId() != null) {
			final List<DataSet> dataSetList =
				this.studyDataManager.getDataSetsByType(meansImportRequest.getStudyId(), DatasetTypeEnum.MEANS_DATA.getId());
			if (CollectionUtils.isNotEmpty(dataSetList)) {
				errors.reject("means.import.means.dataset.already.exists", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}

	}

	protected void checkMeansDataIsEmpty(final MeansImportRequest meansImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());
		if (CollectionUtils.isEmpty(meansImportRequest.getData())) {
			errors.reject("means.import.means.data.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	protected void checkDataValuesIsEmpty(final MeansImportRequest meansImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());
		if (meansImportRequest.getData().stream().anyMatch(d -> MapUtils.isEmpty(d.getValues()))) {
			errors.reject("means.import.means.data.values.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	protected void validateEnvironmentNumbers(final MeansImportRequest meansImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());
		final boolean hasBlankEnvironmentNumbers = meansImportRequest.getData().stream().anyMatch(md -> md.getEnvironmentNumber() == null);
		if (hasBlankEnvironmentNumbers) {
			errors.reject("means.import.means.environment.numbers.required", "");
		} else {
			final Set<Integer> environmentNumbers =
				meansImportRequest.getData().stream().map(MeansImportRequest.MeansData::getEnvironmentNumber).collect(Collectors.toSet());
			final Set<Integer>
				existingEnvironmentNumbers =
				this.studyDataManager.getInstanceGeolocationIdsMap(meansImportRequest.getStudyId()).keySet().stream().map(Integer::valueOf)
					.collect(Collectors.toSet());
			final Collection<Integer> nonExistingEnvironmentNumbers =
				CollectionUtils.subtract(environmentNumbers, existingEnvironmentNumbers);
			if (CollectionUtils.isNotEmpty(nonExistingEnvironmentNumbers)) {
				errors.reject("means.import.means.environment.numbers.do.not.exist",
					new Object[] {StringUtils.join(nonExistingEnvironmentNumbers, ", ")}, "");
			}
		}
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	protected void validateEntryNumber(final MeansImportRequest meansImportRequest, final BindingResult errors) {

		final boolean hasBlankEntryNo = meansImportRequest.getData().stream().anyMatch(md -> md.getEntryNo() == null);
		if (hasBlankEntryNo) {
			errors.reject("means.import.means.entry.numbers.required", "");
		} else {
			final Set<String> entryNumbers =
				meansImportRequest.getData().stream().map(m -> String.valueOf(m.getEntryNo())).collect(Collectors.toSet());

			final StudyEntrySearchDto.Filter filter = new StudyEntrySearchDto.Filter();
			filter.setEntryNumbers(new ArrayList<>(entryNumbers));
			final Set<String>
				existingEntryNumbers =
				this.studyEntryService.getStudyEntries(meansImportRequest.getStudyId(), filter, null).stream()
					.map(e -> String.valueOf(e.getEntryNumber())).collect(
						Collectors.toSet());
			final Collection<String> nonExistingEntryNumbers = CollectionUtils.subtract(entryNumbers, existingEntryNumbers);
			if (CollectionUtils.isNotEmpty(nonExistingEntryNumbers)) {
				errors.reject("means.import.means.entry.numbers.do.not.exist",
					new Object[] {StringUtils.join(nonExistingEntryNumbers, ", ")}, "");
			}

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

	}

	protected void validateAnalysisVariableNames(final MeansImportRequest meansImportRequest, final BindingResult errors) {
		if (CollectionUtils.isNotEmpty(meansImportRequest.getData())) {
			final Set<String> analysisVariableNames =
				meansImportRequest.getData().stream()
					.map(o -> !MapUtils.isEmpty(o.getValues()) ? o.getValues().keySet() : new HashSet<String>()).flatMap(Set::stream)
					.collect(Collectors.toSet());

			final VariableFilter variableFilter = new VariableFilter();
			analysisVariableNames.forEach(variableFilter::addName);
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

	}

}
