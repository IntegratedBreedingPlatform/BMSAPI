
package org.ibp.api.java.impl.middleware.ontology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.TermRelationship;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.TermRelationshipId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.PropertyDetails;
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

/**
 * Validate data of API Services and pass data to middleware services
 */

@Service
public class PropertyServiceImpl extends ServiceBaseImpl implements PropertyService {

	private static final String ERROR_MESSAGE = "Error!";
	private static final String PROPERTY_NAME = "Property";
	private static final String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";

	@Autowired
	private OntologyPropertyDataManager ontologyPropertyDataManager;

	@Autowired
	private PropertyValidator propertyValidator;

	@Override
	public List<PropertyDetails> getAllProperties() {
		try {
			List<Property> propertyList = this.ontologyPropertyDataManager.getAllProperties();
			List<PropertyDetails> properties = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.getInstance();

			for (Property property : propertyList) {
				PropertyDetails propertyDetail = mapper.map(property, PropertyDetails.class);
				properties.add(propertyDetail);
			}
			return properties;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(PropertyServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public PropertyDetails getProperty(String id) {
		this.validateId(id, PropertyServiceImpl.PROPERTY_NAME);
		// Note: Validate Property Id for valid format and property exists or not
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), PropertyServiceImpl.PROPERTY_NAME);
		TermRequest term = new TermRequest(id, PropertyServiceImpl.PROPERTY_NAME, CvId.PROPERTIES.getId());
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
			List<TermRelationship> relationships =
					this.termDataManager.getRelationshipsWithObjectAndType(StringUtil.parseInt(id, null), TermRelationshipId.HAS_PROPERTY);

			Collections.sort(relationships, new Comparator<TermRelationship>() {

				@Override
				public int compare(TermRelationship l, TermRelationship r) {
					return l.getSubjectTerm().getName().compareToIgnoreCase(r.getSubjectTerm().getName());
				}
			});

			for (TermRelationship relationship : relationships) {
				TermSummary termSummary = mapper.map(relationship, TermSummary.class);
				propertyDetails.getMetadata().getUsage().addUsage(termSummary);
			}

			return propertyDetails;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(PropertyServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public GenericResponse addProperty(PropertyDetails propertyDetails) {
		// Note: Set id to null because add property does not need id		
		propertyDetails.setId(null);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Property");
		this.propertyValidator.validate(propertyDetails, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			Property property = new Property();
			property.setName(propertyDetails.getName());
			property.setDefinition(propertyDetails.getDescription());
			property.setCropOntologyId(propertyDetails.getCropOntologyId());
			for (String c : propertyDetails.getClasses()) {
				property.addClass(c);
			}

			this.ontologyPropertyDataManager.addProperty(property);

			return new GenericResponse(String.valueOf(property.getId()));
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(PropertyServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public List<PropertyDetails> getAllPropertiesByClass(String propertyClass) {
		try {
			List<Property> propertyList = this.ontologyPropertyDataManager.getAllPropertiesWithClass(propertyClass);
			List<PropertyDetails> properties = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.getInstance();

			for (Property property : propertyList) {
				PropertyDetails propertyDTO = mapper.map(property, PropertyDetails.class);
				properties.add(propertyDTO);
			}
			return properties;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(PropertyServiceImpl.ERROR_MESSAGE, e);
		}
	}

	@Override
	public void updateProperty(String id, PropertyDetails propertyDetails) {
		this.validateId(id, "Property");
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Property");
		TermRequest term = new TermRequest(id, "property", CvId.PROPERTIES.getId());
		this.termValidator.validate(term, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		propertyDetails.setId(id);

		// Note: Validate property data
		this.propertyValidator.validate(propertyDetails, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			Property property = new Property();
			property.setId(StringUtil.parseInt(id, null));
			property.setName(propertyDetails.getName());
			property.setDefinition(propertyDetails.getDescription());
			property.setCropOntologyId(propertyDetails.getCropOntologyId());

			for (String c : propertyDetails.getClasses()) {
				property.addClass(c);
			}

			this.ontologyPropertyDataManager.updateProperty(property);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void deleteProperty(String id) {
		// Note: Validate Id for valid format and check if property exists or not
		this.validateId(id, "Property");
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Property");

		// Note: Check if property is deletable or not by checking its usage in variable
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(id), "Property", CvId.PROPERTIES.getId()), errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			this.ontologyPropertyDataManager.deleteProperty(StringUtil.parseInt(id, null));
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException(PropertyServiceImpl.ERROR_MESSAGE, e);
		}
	}

}
