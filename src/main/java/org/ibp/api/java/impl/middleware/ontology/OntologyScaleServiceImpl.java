package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyBasicDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.*;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
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
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Autowired
	private OntologyBasicDataManager ontologyBasicDataManager;

	@Override
	public List<ScaleSummary> getAllScales()  {
		try {
			List<Scale> scales = this.ontologyScaleDataManager.getAllScales();
			List<ScaleSummary> scaleSummaries = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.getInstance();

			for (Scale scale : scales) {
				ScaleSummary scaleSummary = mapper.map(scale, ScaleSummary.class);
				scaleSummaries.add(scaleSummary);
			}
			return scaleSummaries;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public ScaleResponse getScaleById(Integer id) {
		try {
			Scale scale = this.ontologyScaleDataManager.getScaleById(id);
			if (scale == null) {
				return null;
			}
			boolean deletable = true;
			if (this.ontologyBasicDataManager.isTermReferred(id)) {
				deletable = false;
			}
			ModelMapper mapper = OntologyMapper.getInstance();
			ScaleResponse response = mapper.map(scale, ScaleResponse.class);
			String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";
			if (!deletable) {
				response.setEditableFields(new ArrayList<>(Collections.singletonList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
			} else {
				response.setEditableFields(new ArrayList<>(Arrays.asList("name", FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED, "validValues")));
			}
			response.setDeletable(deletable);
			return response;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public GenericResponse addScale(ScaleRequest request) {
		try {
			Scale scale = new Scale();
			scale.setName(request.getName().trim());
			scale.setDefinition(request.getDescription().trim());

			Integer dataTypeId = CommonUtil.tryParseSafe(request.getDataTypeId());
			scale.setDataType(DataType.getById(dataTypeId));

			if (Objects.equals(dataTypeId, CATEGORICAL_VARIABLE.getId())) {
				for (VariableCategory category : request.getValidValues().getCategories()) {
					scale.addCategory(category.getName().trim(), category.getDescription().trim());
				}
			}
			if (Objects.equals(dataTypeId, NUMERIC_VARIABLE.getId())) {
				scale.setMinValue(request.getValidValues().getMin().toString());
				scale.setMaxValue(request.getValidValues().getMax().toString());
			}

			this.ontologyScaleDataManager.addScale(scale);
			return new GenericResponse(scale.getId());
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void updateScale(ScaleRequest request) {
		try {
			Scale scale = new Scale(new Term(CommonUtil.tryParseSafe(request.getId()), request.getName().trim(), request.getDescription().trim()));

			Integer dataTypeId = CommonUtil.tryParseSafe(request.getDataTypeId());

			scale.setDataType(DataType.getById(dataTypeId));

			ValidValues validValues = Objects.equals(request.getValidValues(), null) ? new ValidValues() : request.getValidValues();

			if (Objects.equals(dataTypeId, CATEGORICAL_VARIABLE.getId())) {
				for (VariableCategory description : validValues.getCategories()) {
					scale.addCategory(description.getName().trim(), description.getDescription().trim());
				}
			}
			if (Objects.equals(dataTypeId, NUMERIC_VARIABLE.getId())) {
				scale.setMinValue(validValues.getMin().toString());
				scale.setMaxValue(validValues.getMax().toString());
			}

			this.ontologyScaleDataManager.updateScale(scale);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void deleteScale(Integer id) {
		try {
			this.ontologyScaleDataManager.deleteScale(id);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}
}
