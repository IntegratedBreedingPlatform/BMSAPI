package org.ibp.api.java.impl.middleware.ontology.brapi;

import org.generationcp.middleware.api.brapi.VariableTypeGroup;
import org.generationcp.middleware.domain.search_request.brapi.v2.VariableSearchRequestDTO;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.brapi.VariableServiceBrapi;
import org.ibp.api.brapi.v2.variable.VariableImportResponse;
import org.ibp.api.brapi.v2.variable.VariableUpdateResponse;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.VariableDtoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class VariableServiceBrapiImpl implements VariableServiceBrapi {

	@Autowired
	private VariableDtoValidator variableDtoValidator;

	@Autowired
	private org.generationcp.middleware.api.brapi.VariableServiceBrapi middlewareVariableServiceBrapi;

	public List<VariableDTO> getObservationVariables(final String crop, final VariableSearchRequestDTO requestDTO,
		final Pageable pageable) {
		final List<VariableDTO> observationVariables =
			this.middlewareVariableServiceBrapi.getVariables(requestDTO, pageable, VariableTypeGroup.TRAIT);
		observationVariables.forEach(ov -> {
			ov.setCommonCropName(crop);
			ov.setCrop(crop);
		});
		return observationVariables;
	}

	public long countObservationVariables(final VariableSearchRequestDTO requestDTO) {
		return this.middlewareVariableServiceBrapi.countVariables(requestDTO, VariableTypeGroup.TRAIT);
	}

	@Override
	public VariableUpdateResponse updateObservationVariable(final String observationVariableDbId, final VariableDTO variable) {
		final VariableUpdateResponse variableUpdateResponse = new VariableUpdateResponse();
		try {
			this.variableDtoValidator.validateForUpdate(observationVariableDbId, variable);
			variableUpdateResponse.setEntityObject(this.middlewareVariableServiceBrapi.updateObservationVariable(variable));
		} catch (final ApiRequestValidationException e) {
			variableUpdateResponse.setEntityObject(variable);
			variableUpdateResponse.setErrors(e.getErrors());
		}
		return variableUpdateResponse;
	}

	@Override
	public VariableImportResponse createObservationVariables(final String crop, final List<VariableDTO> variableDTOList) {

		final VariableImportResponse response = new VariableImportResponse();
		response.setImportListSize(variableDTOList.size());
		response.setCreatedSize(0);

		final BindingResult bindingResult = this.variableDtoValidator.pruneVariablesInvalidForImport(variableDTOList);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}

		if (!CollectionUtils.isEmpty(variableDTOList)) {
			final List<VariableDTO> variables = this.middlewareVariableServiceBrapi.createObservationVariables(variableDTOList);
			variables.forEach(ov -> {
				ov.setCommonCropName(crop);
				ov.setCrop(crop);
			});
			response.setEntityList(variables);
			response.setCreatedSize(variables.size());
		} else {
			response.setEntityList(new ArrayList<>());
			response.setCreatedSize(0);
		}

		return response;
	}
}
