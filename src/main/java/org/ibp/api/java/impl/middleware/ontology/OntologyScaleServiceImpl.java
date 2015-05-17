package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.ScaleSummary;
import org.ibp.api.domain.ontology.ValidValues;
import org.ibp.api.domain.ontology.VariableCategory;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.ServiceBaseImpl;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.ibp.api.java.impl.middleware.ontology.validator.ScaleValidator;
import org.ibp.api.java.ontology.OntologyScaleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.generationcp.middleware.domain.oms.DataType.CATEGORICAL_VARIABLE;
import static org.generationcp.middleware.domain.oms.DataType.NUMERIC_VARIABLE;

/**
 * Validate data of API Services and pass data to middleware services
 */

@Service
public class OntologyScaleServiceImpl extends ServiceBaseImpl implements OntologyScaleService {

	@Autowired
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Autowired
	private ScaleValidator scaleValidator;

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
	public ScaleDetails getScaleById(String id) {
		validateId(id, "Scale");
		// Note: Validate Scale Id for valid format and scale exists or not
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Scale");
		TermRequest term = new TermRequest(id, "Scale", CvId.SCALES.getId());
		this.termValidator.validate(term, errors);

		// Note: If any error occurs then throws Exception with error messages
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			Scale scale = this.ontologyScaleDataManager.getScaleById(CommonUtil.tryParseSafe(id));
			if (scale == null) {
				return null;
			}
			boolean deletable = true;
			if (this.termDataManager.isTermReferred(CommonUtil.tryParseSafe(id))) {
				deletable = false;
			}
			ModelMapper mapper = OntologyMapper.getInstance();
			ScaleDetails scaleDetails = mapper.map(scale, ScaleDetails.class);
			String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";
			if (!deletable) {
				scaleDetails.getMetadata().setEditableFields(new ArrayList<>(Collections.singletonList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
			} else {
				scaleDetails.getMetadata().setEditableFields(new ArrayList<>(Arrays.asList("name", FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED, "validValues")));
			}
			scaleDetails.getMetadata().setDeletable(deletable);
			return scaleDetails;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public GenericResponse addScale(ScaleSummary scaleSummary) {
		// Note: Set id to null because add scale does not need id
		scaleSummary.setId(null);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Scale");
		this.scaleValidator.validate(scaleSummary, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			Scale scale = new Scale();
			scale.setName(scaleSummary.getName().trim());
			scale.setDefinition(scaleSummary.getDescription().trim());

			Integer dataTypeId = scaleSummary.getDataType().getId();
			scale.setDataType(DataType.getById(dataTypeId));

			if (Objects.equals(dataTypeId, CATEGORICAL_VARIABLE.getId())) {
				for (VariableCategory category : scaleSummary.getValidValues().getCategories()) {
					scale.addCategory(category.getName().trim(), category.getDescription().trim());
				}
			}
			if (Objects.equals(dataTypeId, NUMERIC_VARIABLE.getId())) {
				scale.setMinValue(scaleSummary.getValidValues().getMin().toString());
				scale.setMaxValue(scaleSummary.getValidValues().getMax().toString());
			}

			this.ontologyScaleDataManager.addScale(scale);
			return new GenericResponse(scale.getId());
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void updateScale(String id,ScaleSummary scaleSummary) {
		validateId(id, "Scale");
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Scale");
		TermRequest term = new TermRequest(id, "scale", CvId.SCALES.getId());
		this.termValidator.validate(term, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		scaleSummary.setId(id);

		// Note: Validate scale data
		this.scaleValidator.validate(scaleSummary, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			Scale scale = new Scale(new Term(CommonUtil.tryParseSafe(scaleSummary.getId()), scaleSummary.getName().trim(), scaleSummary.getDescription().trim()));

			Integer dataTypeId = scaleSummary.getDataType().getId();

			scale.setDataType(DataType.getById(dataTypeId));

			ValidValues validValues = Objects.equals(scaleSummary.getValidValues(), null) ? new ValidValues() : scaleSummary.getValidValues();

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
	public void deleteScale(String id) {
		// Note: Validate Id for valid format and check if scale exists or not
		validateId(id, "Scale");
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Scale");

		// Note: Check if scale is deletable or not by checking its usage in variable
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(id), "Scale", CvId.SCALES.getId()), errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			this.ontologyScaleDataManager.deleteScale(CommonUtil.tryParseSafe(id));
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

}
