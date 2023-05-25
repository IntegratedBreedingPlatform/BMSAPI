package org.ibp.api.java.impl.middleware.genotype;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.api.genotype.SampleGenotypeService;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.genotype.SampleGenotypeImportRequestDto;
import org.generationcp.middleware.domain.genotype.SampleGenotypeVariablesSearchFilter;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.service.api.SampleService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.domain.ontology.Category;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SampleGenotypeValidator {

	// Maximum number of genotype marker variables per sample list
	public static final int MAX_NUMBER_OF_VARIABLES_PER_SAMPLE_LIST = 20;

	@Autowired
	private VariableService variableService;

	@Autowired
	private SampleService sampleService;

	@Autowired
	private SampleGenotypeService sampleGenotypeServiceMiddleware;

	protected BindingResult errors;

	// Validate the import request
	public void validateImport(final String programUUID, final Integer studyId, final Integer sampleListId,
		final List<SampleGenotypeImportRequestDto> sampleGenotypeImportRequestDtoList) {
		BaseValidator.checkNotEmpty(sampleGenotypeImportRequestDtoList, "genotype.import.request.null");
		this.errors = new MapBindingResult(new HashMap<>(), StudyImportRequestDTO.class.getName());

		final Set<String> sampleUID = sampleGenotypeImportRequestDtoList.stream()
			.map(SampleGenotypeImportRequestDto::getSampleUID).collect(Collectors.toSet());
		final Map<String, SampleDTO> sampleDTOMap = this.sampleService.getSamplesBySampleUID(sampleUID);
		if (sampleUID.size() != sampleDTOMap.size()) {
			this.errors.reject("sample.uids.not.exist", "Some sampleUIDs were not found in the system. Please check");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		this.validateVariableIds(studyId, sampleListId, programUUID, sampleGenotypeImportRequestDtoList);
	}

	// Validate the variable ids in the import request
	public void validateVariableIds(final Integer studyId, final Integer sampleListId, final String programUUID,
		final List<SampleGenotypeImportRequestDto> sampleGenotypeImportRequestDtoList) {
		final Set<Integer> variableIds = sampleGenotypeImportRequestDtoList.stream()
			.map(genotypeImportRequestDto -> Integer.valueOf(genotypeImportRequestDto.getVariableId())).collect(Collectors.toSet());
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addVariableIds(new ArrayList<>(variableIds));
		variableFilter.addVariableType(VariableType.GENOTYPE_MARKER.getId());
		variableFilter.setProgramUuid(programUUID);
		final Map<String, VariableDetails> variables = this.variableService.getVariablesByFilter(variableFilter).stream()
			.collect(Collectors.toMap(VariableDetails::getId, Function.identity()));

		// Check if the variables in specified in the request are existing
		// If not, throw an error
		if (variables.size() != variableIds.size()) {
			this.errors.reject("genotype.import.variable.id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		// Get the existing genotype marker variables in the sample list
		final SampleGenotypeVariablesSearchFilter filter = new SampleGenotypeVariablesSearchFilter();
		filter.setStudyId(studyId);
		filter.setSampleListIds(Arrays.asList(sampleListId));
		final Map<Integer, MeasurementVariable> existingGenotypeVariablesInSampleList =
			this.sampleGenotypeServiceMiddleware.getSampleGenotypeVariables(filter);

		final Set<Integer> variablesIdsFromRequest =
			variables.keySet().stream().map(Integer::parseInt).collect(Collectors.toSet());
		final Set<Integer> variableIdsFromTheSampleList = new HashSet<>(existingGenotypeVariablesInSampleList.keySet());

		final Set<Integer> uncommonVariableIds = new HashSet<>(variablesIdsFromRequest);
		uncommonVariableIds.removeAll(variableIdsFromTheSampleList);

		// If the import request has new variables that are not yet existing in the sample list,
		// we have to make sure that the new variables that will be added + number of existing variables will not exceed the
		// maximum number of genotype variables allowed per sample list.
		// Otherwise, we will throw an error.
		if (variableIdsFromTheSampleList.size() + uncommonVariableIds.size() > MAX_NUMBER_OF_VARIABLES_PER_SAMPLE_LIST) {
			this.errors.reject("genotype.import.variables.exceed.maximum.limit.per.sample.list",
				new Object[] {MAX_NUMBER_OF_VARIABLES_PER_SAMPLE_LIST}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		// Validate the genotype value based on the scale type of the variable.
		for (final SampleGenotypeImportRequestDto importData : sampleGenotypeImportRequestDtoList) {
			final VariableDetails variable = variables.get(importData.getVariableId());
			if (!this.isDataValid(importData, variable)) {
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}

	}

	// Validate the genotype value based on the scale type of the variable.
	private boolean isDataValid(final SampleGenotypeImportRequestDto importData, final VariableDetails variable) {
		if (StringUtils.isEmpty(importData.getValue())) {
			return true;
		}

		final ScaleDetails scale = variable.getScale();
		final org.ibp.api.domain.ontology.DataType scaleDataType = scale.getDataType();
		final String scaleDataTypeId = scaleDataType.getId();
		final String value = importData.getValue();

		if (Objects.equals(DataType.CATEGORICAL_VARIABLE.getId().toString(), scaleDataTypeId)) {
			final List<Category> categories = scale.getValidValues().getCategories();
			if (CollectionUtils.isNotEmpty(categories) && categories.stream().noneMatch(category -> category.getName().equals(value))) {
				this.errors.reject("genotype.import.variable.values.invalid.categorical.out.of.bounds",
					new String[] {variable.getName(), categories.stream().map(Category::getName).collect(Collectors.joining(","))}, "");
				return false;
			}
		}

		if (Objects.equals(DataType.NUMERIC_VARIABLE.getId().toString(), scaleDataTypeId)) {
			if (!StringUtils.isNumeric(value)) {
				this.errors.reject("genotype.import.variable.values.invalid.numerical", new String[] {variable.getName()}, "");
				return false;
			}

			if (variable.getExpectedRange() != null) {
				final String min = variable.getExpectedRange().getMin();
				final String max = variable.getExpectedRange().getMax();
				if (StringUtils.isNotEmpty(min) && StringUtils.isNotEmpty(max) && StringUtils.isNumeric(value)) {
					final int intValue = Integer.parseInt(value);
					final int minVal = Integer.parseInt(min);
					final int maxVal = Integer.parseInt(max);
					if (intValue > maxVal || intValue < minVal) {
						this.errors.reject("genotype.import.variable.values.invalid.numerical.not.within.expected.range",
							new String[] {variable.getName(), min, max}, "");
						return false;
					}
				}
			}
		}

		if (Objects.equals(DataType.DATE_TIME_VARIABLE.getId().toString(), scaleDataTypeId)
			&& Util.tryParseDate(value, Util.DATE_AS_NUMBER_FORMAT) == null) {
			this.errors.reject("genotype.import.variable.values.invalid.date", new String[] {variable.getName()}, "");
			return false;
		}

		return true;
	}

}
