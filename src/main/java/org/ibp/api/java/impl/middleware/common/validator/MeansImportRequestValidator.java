package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.pojos.dms.StockModel;
import org.generationcp.middleware.service.impl.analysis.MeansImportRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.thymeleaf.util.MapUtils;

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
		// Validate environmentId
		this.validateEnvironmentId(meansImportRequest);
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

	protected void validateEnvironmentId(final MeansImportRequest meansImportRequest) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), MeansImportRequest.class.getName());
		final boolean hasBlankEnvironmentId = meansImportRequest.getData().stream().anyMatch(md -> md.getEnvironmentId() == null);
		if (hasBlankEnvironmentId) {
			errors.reject("means.import.means.environment.ids.required", "");
		} else {
			final Set<Integer> environmentIds =
				meansImportRequest.getData().stream().map(MeansImportRequest.MeansData::getEnvironmentId).collect(Collectors.toSet());
			final Set<Integer>
				existingEnvironmentIds =
				this.studyDataManager.getInstanceGeolocationIdsMap(meansImportRequest.getStudyId()).values().stream().collect(
					Collectors.toSet());
			final Collection<Integer> nonExistingEnvironmentIds = CollectionUtils.subtract(environmentIds, existingEnvironmentIds);
			if (CollectionUtils.isNotEmpty(nonExistingEnvironmentIds)) {
				errors.reject("means.import.means.environment.ids.do.not.exist",
					new Object[] {StringUtils.join(nonExistingEnvironmentIds, ", ")}, "");
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
			final Set<String>
				existingEntryNumbers =
				this.studyDataManager.getStocksByStudyAndEntryNumbers(meansImportRequest.getStudyId(), entryNumbers).stream()
					.map(StockModel::getUniqueName).collect(
						Collectors.toSet());
			final Collection<String> nonExistingEntryNumbers = CollectionUtils.subtract(entryNumbers, existingEntryNumbers);
			if (CollectionUtils.isNotEmpty(nonExistingEntryNumbers)) {
				errors.reject("means.import.means.entry.numbers.do.not.exist",
					new Object[] {StringUtils.join(nonExistingEntryNumbers, ", ")}, "");
			}

			final Map<Integer, List<MeansImportRequest.MeansData>> meansDataGroupByEnvironmentIdMap = meansImportRequest.
				getData().stream().collect(Collectors.groupingBy(MeansImportRequest.MeansData::getEnvironmentId, Collectors.toList()));
			final boolean hasDuplicateEntryNumbersPerEnvironment =
				meansDataGroupByEnvironmentIdMap.values().stream().anyMatch(
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
