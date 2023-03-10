package org.ibp.api.java.impl.middleware.genotype;

import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.domain.genotype.SampleGenotypeImportRequestDto;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.rest.sample.SampleListValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GenotypeValidator {

	@Autowired
	private VariableService variableService;

	@Autowired
	private SampleListValidator sampleListValidator;

	protected BindingResult errors;

	public void validateImport(final String programUUID,
		final List<SampleGenotypeImportRequestDto> sampleGenotypeImportRequestDtoList) {
		BaseValidator.checkNotEmpty(sampleGenotypeImportRequestDtoList, "genotype.import.request.null");
		this.errors = new MapBindingResult(new HashMap<>(), StudyImportRequestDTO.class.getName());
		final Set<Integer> sampleIds = sampleGenotypeImportRequestDtoList.stream()
			.map(genotypeImportRequestDto -> Integer.valueOf(genotypeImportRequestDto.getSampleId())).collect(Collectors.toSet());
		this.sampleListValidator.verifySamplesExist(new ArrayList<>(sampleIds));
		this.validateVariableIds(programUUID, sampleGenotypeImportRequestDtoList);
	}

	public void validateVariableIds(final String programUUID,
		final List<SampleGenotypeImportRequestDto> sampleGenotypeImportRequestDtoList) {
		final Set<Integer> variableIds = sampleGenotypeImportRequestDtoList.stream()
			.map(genotypeImportRequestDto -> Integer.valueOf(genotypeImportRequestDto.getVariableId())).collect(Collectors.toSet());
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addVariableIds(new ArrayList<>(variableIds));
		variableFilter.addVariableType(VariableType.GENOTYPE_MARKER.getId());
		variableFilter.setProgramUuid(programUUID);
		final List<VariableDetails> variables = this.variableService.getVariablesByFilter(variableFilter);
		if (variables.size() != variableIds.size()) {
			this.errors.reject("genotype.import.variable.id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}
}
