package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.domain.ontology.PropertySummary;
import org.ibp.api.domain.ontology.TermRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.ibp.api.java.impl.middleware.ontology.validator.MiddlewareIdFormatValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.PropertyValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.ontology.OntologyPropertyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Validate data of API Services and pass data to middleware services
 */

@Service
public class OntologyPropertyServiceImpl implements OntologyPropertyService {

	@Autowired
	private OntologyPropertyDataManager ontologyPropertyDataManager;
	@Autowired
	private TermDataManager termDataManager;

	@Autowired
	private PropertyValidator propertyValidator;

	@Autowired
	protected TermDeletableValidator termDeletableValidator;

	@Autowired
	protected MiddlewareIdFormatValidator idFormatValidator;

	@Autowired
	protected TermValidator termValidator;

  	@Override
	public List<PropertySummary> getAllProperties() {
		try {
			List<Property> propertyList = this.ontologyPropertyDataManager.getAllProperties();
			List<PropertySummary> properties = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.getInstance();

			for (Property property : propertyList) {
			  	PropertySummary propertyDTO = mapper.map(property, PropertySummary.class);
			  	properties.add(propertyDTO);
			}
			return properties;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	public PropertyDetails getProperty(String id) {
		// Note: Validate Property Id for valid format and property exists or not
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Property");
		TermRequest term = new TermRequest(id, "Property", CvId.PROPERTIES.getId());
		this.termValidator.validate(term, errors);

		// Note: If any error occurs then throws Exception with error messages
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			Property property = this.ontologyPropertyDataManager.getProperty(CommonUtil.tryParseSafe(id));
			if (property == null) {
			  	return null;
			}
			boolean deletable = true;
			if (this.termDataManager.isTermReferred(CommonUtil.tryParseSafe(id))) {
			  	deletable = false;
			}
			ModelMapper mapper = OntologyMapper.getInstance();
			PropertyDetails response = mapper.map(property, PropertyDetails.class);

			String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";

			// Note: If property is used then description is editable else all fields can be editable
			if (!deletable) {
			  	response.getMetadata().setEditableFields(new ArrayList<>(Arrays.asList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED, "classes", "cropOntologyId")));
			} else {
			  	response.getMetadata().setEditableFields(new ArrayList<>(Arrays.asList("name", FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED,
						"classes", "cropOntologyId")));
			}
			response.getMetadata().setDeletable(deletable);
			return response;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public GenericResponse addProperty(PropertySummary propertySummary) {
		// Note: Set id to null because add property does not need id
		propertySummary.setId(null);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Property");
		this.propertyValidator.validate(propertySummary, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			Property property = new Property();
			property.setName(propertySummary.getName());
			property.setDefinition(propertySummary.getDescription());
			property.setCropOntologyId(propertySummary.getCropOntologyId());
			for (String c : propertySummary.getClasses()) {
			  	property.addClass(c);
			}

			this.ontologyPropertyDataManager.addProperty(property);

			return new GenericResponse(property.getId());
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public List<PropertySummary> getAllPropertiesByClass(String propertyClass) {
		try {
			List<Property> propertyList = this.ontologyPropertyDataManager.getAllPropertiesWithClass(propertyClass);
			List<PropertySummary> properties = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.getInstance();

			for (Property property : propertyList) {
			  	PropertySummary propertyDTO = mapper.map(property, PropertySummary.class);
			  	properties.add(propertyDTO);
			}
			return properties;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void deleteProperty(String id) {
		// Note: Validate Id for valid format and check if property exists or not
		validateId(id);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Property");

		// Note: Check if property is deletable or not by checking its usage in variable
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(id), "Property", CvId.PROPERTIES.getId()), errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			this.ontologyPropertyDataManager.deleteProperty(CommonUtil.tryParseSafe(id));
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void updateProperty(String id, PropertySummary propertySummary) {
		validateId(id);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Property");
		TermRequest term = new TermRequest(id, "property", CvId.PROPERTIES.getId());
		this.termValidator.validate(term, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		propertySummary.setId(id);

		// Note: Validate property data
		this.propertyValidator.validate(propertySummary, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			Property property = new Property();
			property.setId(CommonUtil.tryParseSafe(id));
			property.setName(propertySummary.getName());
			property.setDefinition(propertySummary.getDescription());
			property.setCropOntologyId(propertySummary.getCropOntologyId());

			for (String c : propertySummary.getClasses()) {
			  	property.addClass(c);
			}

			this.ontologyPropertyDataManager.updateProperty(property);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	// Note: Used for validating id format and id exists or not
	private void validateId(String id) {
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Property");
		this.idFormatValidator.validate(id, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}
}
