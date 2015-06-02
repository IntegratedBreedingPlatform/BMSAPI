package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.TermRelationship;
import org.generationcp.middleware.domain.oms.TermRelationshipId;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.domain.ontology.PropertySummary;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.ServiceBaseImpl;
import org.ibp.api.java.impl.middleware.ontology.validator.PropertyValidator;
import org.ibp.api.java.ontology.PropertyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Validate data of API Services and pass data to middleware services
 */

@Service
public class PropertyServiceImpl extends ServiceBaseImpl implements PropertyService {

	@Autowired
	private OntologyPropertyDataManager ontologyPropertyDataManager;

	@Autowired
	private PropertyValidator propertyValidator;

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
		validateId(id, "Property");
		// Note: Validate Property Id for valid format and property exists or not
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Property");
		TermRequest term = new TermRequest(id, "Property", CvId.PROPERTIES.getId());
		this.termValidator.validate(term, errors);

		// Note: If any error occurs then throws Exception with error messages
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			Property property = this.ontologyPropertyDataManager.getProperty(StringUtil.parseInt(id, null));
			if (property == null) {
			  	return null;
			}
			boolean deletable = true;
			if (this.termDataManager.isTermReferred(StringUtil.parseInt(id, null))) {
			  	deletable = false;
			}
			ModelMapper mapper = OntologyMapper.getInstance();
			PropertyDetails propertyDetails = mapper.map(property, PropertyDetails.class);

			String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";

			// Note: If property is used then description is editable else all fields can be editable
			if (!deletable) {
				propertyDetails.getMetadata().addEditableField(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED);
				propertyDetails.getMetadata().addEditableField("classes");
				propertyDetails.getMetadata().addEditableField("cropOntologyId");
			} else {
				propertyDetails.getMetadata().addEditableField("name");
				propertyDetails.getMetadata().addEditableField(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED);
				propertyDetails.getMetadata().addEditableField("classes");
				propertyDetails.getMetadata().addEditableField("cropOntologyId");
			}
			propertyDetails.getMetadata().setDeletable(deletable);

			// Note : Get list of relationships related to property Id
			List<TermRelationship> relationships = termDataManager.getRelationshipsWithObjectAndType(StringUtil.parseInt(id, null), TermRelationshipId.HAS_PROPERTY);

			for(TermRelationship relationship : relationships){
                TermSummary termSummary = mapper.map(relationship, TermSummary.class);
                propertyDetails.getMetadata().getUsage().addUsage(termSummary);
            }

			return propertyDetails;
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

			return new GenericResponse(String.valueOf(property.getId()));
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
		validateId(id, "Property");
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Property");

		// Note: Check if property is deletable or not by checking its usage in variable
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(id), "Property", CvId.PROPERTIES.getId()), errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			this.ontologyPropertyDataManager.deleteProperty(StringUtil.parseInt(id, null));
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void updateProperty(String id, PropertySummary propertySummary) {
		validateId(id, "Property");
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
			property.setId(StringUtil.parseInt(id, null));
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

}
