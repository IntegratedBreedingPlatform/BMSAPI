package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyBasicDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.PropertyRequest;
import org.ibp.api.domain.ontology.PropertyResponse;
import org.ibp.api.domain.ontology.PropertySummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.ontology.OntologyPropertyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OntologyPropertyServiceImpl implements OntologyPropertyService {

	@Autowired
	private OntologyPropertyDataManager ontologyPropertyDataManager;
	@Autowired
	private OntologyBasicDataManager ontologyBasicDataManager;

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

	@Override
	public PropertyResponse getProperty(Integer id) {
		try {
			Property property = this.ontologyPropertyDataManager.getProperty(id);
			if (property == null) {
			  	return null;
			}
			boolean deletable = true;
			if (this.ontologyBasicDataManager.isTermReferred(id)) {
			  	deletable = false;
			}
			ModelMapper mapper = OntologyMapper.getInstance();
			PropertyResponse response = mapper.map(property, PropertyResponse.class);

			String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";

			if (!deletable) {
			  	response.setEditableFields(new ArrayList<>(Arrays.asList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED, "classes", "cropOntologyId")));
			} else {
			  	response.setEditableFields(new ArrayList<>(Arrays.asList("name", FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED,
					  "classes", "cropOntologyId")));
			}
			response.setDeletable(deletable);
			return response;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public GenericResponse addProperty(PropertyRequest request) {
		try {
			Property property = new Property();
			property.setName(request.getName());
			property.setDefinition(request.getDescription());
			property.setCropOntologyId(request.getCropOntologyId());

			for (String c : request.getClasses()) {
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
	public boolean deleteProperty(Integer id) {
		try {
			boolean isReferred = this.ontologyBasicDataManager.isTermReferred(id);
			if (isReferred) {
			  	return false;
			}
			this.ontologyPropertyDataManager.deleteProperty(id);
			return true;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void updateProperty(Integer id, PropertyRequest request) {
		try {
			Property property = new Property();
			property.setId(id);
			property.setName(request.getName());
			property.setDefinition(request.getDescription());
			property.setCropOntologyId(request.getCropOntologyId());

			for (String c : request.getClasses()) {
			  	property.addClass(c);
			}

			this.ontologyPropertyDataManager.updateProperty(property);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}
}
