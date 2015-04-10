package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.*;
import org.ibp.api.java.ontology.OntologyScaleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.generationcp.middleware.domain.oms.DataType.CATEGORICAL_VARIABLE;
import static org.generationcp.middleware.domain.oms.DataType.NUMERIC_VARIABLE;

@Service
public class OntologyScaleServiceImpl implements OntologyScaleService {

	@Autowired
	private OntologyManagerService ontologyManagerService;

	@Override
	public List<ScaleSummary> getAllScales() throws MiddlewareQueryException {
		List<Scale> scales = this.ontologyManagerService.getAllScales();
		List<ScaleSummary> scaleSummaries = new ArrayList<>();

		ModelMapper mapper = OntologyMapper.scaleMapper();

		for (Scale scale : scales) {
			ScaleSummary scaleSummary = mapper.map(scale, ScaleSummary.class);
			scaleSummaries.add(scaleSummary);
		}
		return scaleSummaries;
	}

	@Override
	public ScaleResponse getScaleById(Integer id) throws MiddlewareQueryException {
		Scale scale = this.ontologyManagerService.getScaleById(id);
		if (scale == null) {
			return null;
		}
		boolean deletable = true;
		if (this.ontologyManagerService.isTermReferred(id)) {
			deletable = false;
		}
		ModelMapper mapper = OntologyMapper.scaleMapper();
		ScaleResponse response = mapper.map(scale, ScaleResponse.class);
		String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";
		if (!deletable) {
			response.setEditableFields(new ArrayList<>(Collections.singletonList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
		} else {
			response.setEditableFields(new ArrayList<>(Arrays.asList("name", FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED, "validValues")));
		}
		response.setDeletable(deletable);
		return response;
	}

	@Override
	public GenericResponse addScale(ScaleRequest request) throws MiddlewareQueryException, MiddlewareException {
		Scale scale = new Scale();
		scale.setName(request.getName());
		scale.setDefinition(request.getDescription());

		scale.setDataType(DataType.getById(request.getDataTypeId()));

		if (Objects.equals(request.getDataTypeId(), CATEGORICAL_VARIABLE.getId())) {
			for (NameDescription description : request.getValidValues().getCategories()) {
				scale.addCategory(description.getName(), description.getDescription());
			}
		}
		if (Objects.equals(request.getDataTypeId(), NUMERIC_VARIABLE.getId())) {
			scale.setMinValue(request.getValidValues().getMin());
			scale.setMaxValue(request.getValidValues().getMax());
		}

		this.ontologyManagerService.addScale(scale);
		return new GenericResponse(scale.getId());
	}

	@Override
	public void updateScale(ScaleRequest request) throws MiddlewareQueryException,
			MiddlewareException {
		Scale scale = new Scale(new Term(request.getId(), request.getName(),
				request.getDescription()));

		scale.setDataType(DataType.getById(request.getDataTypeId()));

		ValidValues validValues = Objects.equals(request.getValidValues(), null) ? new ValidValues()
				: request.getValidValues();

		if (Objects.equals(request.getDataTypeId(), CATEGORICAL_VARIABLE.getId())) {
			for (NameDescription description : validValues.getCategories()) {
				scale.addCategory(description.getName(), description.getDescription());
			}
		}
		if (Objects.equals(request.getDataTypeId(), NUMERIC_VARIABLE.getId())) {
			scale.setMinValue(validValues.getMin());
			scale.setMaxValue(validValues.getMax());
		}

		this.ontologyManagerService.updateScale(scale);
	}

	@Override
	public void deleteScale(Integer id) throws MiddlewareQueryException, MiddlewareException {
		this.ontologyManagerService.deleteScale(id);
	}
}
