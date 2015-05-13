package org.ibp.api.java.ontology;

import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.domain.ontology.PropertySummary;

import java.util.List;

public interface OntologyPropertyService {

	/**
	 * get all properties
	 *
	 * @return list of properties
	 */
	List<PropertySummary> getAllProperties();

	/**
	 * get property by given property id
	 *
	 * @param id property id
	 * @return property that have given id
	 */
	PropertyDetails getProperty(String id);

	/**
	 * add property using given input data
	 *
	 * @param request property data to be added
	 * @return newly created property id
	 */
	GenericResponse addProperty(PropertySummary request);

	/**
	 * get all properties containing class name
	 *
	 * @param propertyClass class name to be search in property
	 * @return list of properties
	 */
	List<PropertySummary> getAllPropertiesByClass(String propertyClass);

	/**
	 * delete property if not used
	 *
	 * @param id property to be deleted
	 */
	void deleteProperty(String id);

	/**
	 *
	 * @param id property to update
	 * @param request property data to update
	 */
	void updateProperty(String id, PropertySummary request);
}
