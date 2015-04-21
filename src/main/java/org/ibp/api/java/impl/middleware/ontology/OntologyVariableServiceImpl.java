package org.ibp.api.java.impl.middleware.ontology;

import com.google.common.base.Strings;

import org.generationcp.middleware.domain.oms.OntologyVariable;
import org.generationcp.middleware.domain.oms.OntologyVariableInfo;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.api.CommonUtil;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.VariableRequest;
import org.ibp.api.domain.ontology.VariableResponse;
import org.ibp.api.domain.ontology.VariableSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.ontology.OntologyVariableService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class OntologyVariableServiceImpl implements OntologyVariableService {

    @Autowired
    private OntologyManagerService ontologyManagerService;

    @Override
    public List<VariableSummary> getAllVariablesByFilter(String programId, Integer propertyId, Boolean favourite) {
        try {
			List<OntologyVariableSummary> variableSummaries = this.ontologyManagerService.getWithFilter(programId, favourite, null, propertyId, null);
			List<VariableSummary> variableSummaryList = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.variableMapper();

			for (OntologyVariableSummary variable : variableSummaries) {
			    VariableSummary variableSummary = mapper.map(variable, VariableSummary.class);
			    variableSummaryList.add(variableSummary);
			}
			return variableSummaryList;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
    }

    @Override
    public VariableResponse getVariableById(String programId, Integer variableId) {
        try {
			OntologyVariable ontologyVariable = this.ontologyManagerService.getVariable(programId, variableId);

			if (ontologyVariable == null) {
			    return null;
			}

			boolean deletable = true;
			if (this.ontologyManagerService.isTermReferred(variableId)) {
			    deletable = false;
			}

			ModelMapper mapper = OntologyMapper.variableResponseMapper();
			VariableResponse response = mapper.map(ontologyVariable, VariableResponse.class);

			if (!deletable) {
			    response.setEditableFields(new ArrayList<>(Collections.singletonList("description")));
			} else {
			    response.setEditableFields(new ArrayList<>(Arrays.asList("name", "description", "alias",
			            "cropOntologyId", "variableTypeIds", "propertySummary", "methodSummary", "scale", "expectedRange")));
			}
			response.setDeletable(deletable);
			return response;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
    }

    @Override
    public GenericResponse addVariable(VariableRequest request) {

        try {
			Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
			Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
			Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

			OntologyVariableInfo variableInfo = new OntologyVariableInfo();
			variableInfo.setName(request.getName());
			variableInfo.setDescription(request.getDescription());
			variableInfo.setMethodId(methodId);
			variableInfo.setPropertyId(propertyId);
			variableInfo.setScaleId(scaleId);

			if (!Strings.isNullOrEmpty(request.getExpectedRange().getMin()) && !Strings.isNullOrEmpty(request.getExpectedRange().getMax())) {
			    variableInfo.setMinValue(request.getExpectedRange().getMin());
			    variableInfo.setMaxValue(request.getExpectedRange().getMax());
			}

			for (String i : request.getVariableTypeIds()) {
			    variableInfo.addVariableType(VariableType.getById(CommonUtil.tryParseSafe(i)));
			}

			this.ontologyManagerService.addVariable(variableInfo);
			return new GenericResponse(variableInfo.getId());
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
    }

    @Override
    public void updateVariable(VariableRequest request) {
        try {
			Integer id = CommonUtil.tryParseSafe(request.getId());
			Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
			Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());
			Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());

			OntologyVariableInfo variableInfo = new OntologyVariableInfo();
			variableInfo.setId(id);
			variableInfo.setProgramUuid(request.getProgramUuid());
			variableInfo.setName(request.getName());
			variableInfo.setAlias(request.getAlias());
			variableInfo.setDescription(request.getDescription());
			variableInfo.setMethodId(methodId);
			variableInfo.setPropertyId(propertyId);
			variableInfo.setScaleId(scaleId);
			variableInfo.setIsFavorite(request.isFavourite());

			if (!Strings.isNullOrEmpty(request.getExpectedRange().getMin()) && !Strings.isNullOrEmpty(request.getExpectedRange().getMax())) {
			    variableInfo.setMinValue(request.getExpectedRange().getMin());
			    variableInfo.setMaxValue(request.getExpectedRange().getMax());
			}

			for (String i : request.getVariableTypeIds()) {
			    variableInfo.addVariableType(VariableType.getById(CommonUtil.tryParseSafe(i)));
			}

			this.ontologyManagerService.updateVariable(variableInfo);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
    }
}
