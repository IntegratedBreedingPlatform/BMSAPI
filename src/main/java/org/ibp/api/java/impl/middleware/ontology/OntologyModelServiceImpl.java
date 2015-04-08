package org.ibp.api.java.impl.middleware.ontology;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import org.generationcp.middleware.domain.oms.*;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.domain.ontology.*;
import org.ibp.api.java.ontology.OntologyModelService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class OntologyModelServiceImpl implements OntologyModelService {

	@Autowired
	private OntologyManagerService ontologyManagerService;

  @Override
	public List<IdName> getAllDataTypes() throws MiddlewareQueryException {
		return Util.convertAll(Arrays.asList(DataType.values()), new Function<DataType, IdName>() {

			@Override
			public IdName apply(DataType dataType) {
				return new IdName(dataType.getId(), dataType.getName());
			}
		});
	}

	@Override
	public List<String> getAllClasses() throws MiddlewareQueryException {
		List<Term> classes = this.ontologyManagerService.getAllTraitClass();
		List<String> classList = new ArrayList<>();

		for (Term term : classes) {
			classList.add(term.getName());
		}
		return classList;
	}

	@Override
	public List<VariableTypeResponse> getAllVariableTypes() {

		return Util.convertAll(Arrays.asList(VariableType.values()),
				new Function<VariableType, VariableTypeResponse>() {

					@Override
					public VariableTypeResponse apply(VariableType variableType) {
						return new VariableTypeResponse(variableType.getId(), variableType
								.getName(), variableType.getDescription());
					}
				});
	}

	@Override
	public List<VariableSummary> getAllVariablesByFilter(Integer programId, Integer propertyId,
			Boolean favourite) throws MiddlewareQueryException {
		List<OntologyVariableSummary> variableSummaries = this.ontologyManagerService
				.getWithFilter(programId, favourite, null, propertyId, null);
		List<VariableSummary> variableSummaryList = new ArrayList<>();

		ModelMapper mapper = OntologyMapper.variableMapper();

		for (OntologyVariableSummary variable : variableSummaries) {
			VariableSummary variableSummary = mapper.map(variable, VariableSummary.class);
			variableSummaryList.add(variableSummary);
		}
		return variableSummaryList;
	}

	@Override
	public VariableResponse getVariableById(Integer programId, Integer variableId)
			throws MiddlewareQueryException, MiddlewareException {
		OntologyVariable ontologyVariable = this.ontologyManagerService.getVariable(programId,
				variableId);
		if (ontologyVariable == null) {
			return null;
		}
		boolean deletable = true;
		if (this.ontologyManagerService.isTermReferred(variableId)) {
			deletable = false;
		}
		ModelMapper mapper = OntologyMapper.variableResponseMapper();
		VariableResponse response = mapper.map(ontologyVariable, VariableResponse.class);
	  String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";
	  if (!deletable) {
		  response.setEditableFields(new ArrayList<>(Collections
					.singletonList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
		} else {
			response.setEditableFields(new ArrayList<>(Arrays.asList("name", FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED,
					"alias", "cropOntologyId", "variableTypeIds", "propertySummary",
					"methodSummary", "scale", "expectedRange")));
		}
		response.setDeletable(deletable);
		return response;
	}

	@Override
	public GenericResponse addVariable(VariableRequest request) throws MiddlewareQueryException,
			MiddlewareException {
		OntologyVariableInfo variableInfo = new OntologyVariableInfo();
		variableInfo.setName(request.getName());
		variableInfo.setDescription(request.getDescription());
		variableInfo.setMethodId(request.getMethodId());
		variableInfo.setPropertyId(request.getPropertyId());
		variableInfo.setScaleId(request.getScaleId());

		if (!Strings.isNullOrEmpty(request.getExpectedRange().getMin())
				&& !Strings.isNullOrEmpty(request.getExpectedRange().getMax())) {
			variableInfo.setMinValue(request.getExpectedRange().getMin());
			variableInfo.setMaxValue(request.getExpectedRange().getMax());
		}

		for (Integer i : request.getVariableTypeIds()) {
			variableInfo.addVariableType(VariableType.getById(i));
		}
		this.ontologyManagerService.addVariable(variableInfo);
		return new GenericResponse(variableInfo.getId());
	}
}
