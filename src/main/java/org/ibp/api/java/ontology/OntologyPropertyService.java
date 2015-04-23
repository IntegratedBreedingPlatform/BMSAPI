package org.ibp.api.java.ontology;

import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.PropertyRequest;
import org.ibp.api.domain.ontology.PropertyResponse;
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
	PropertyResponse getProperty(Integer id);

	/**
	 * add property using given input data
	 *
	 * @param request property data to be added
	 * @return newly created property id
	 */
	GenericResponse addProperty(PropertyRequest request);

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
	 * @return if property deleted or not
	 */
	boolean deleteProperty(Integer id);

	/**
	 *
	 * @param id property to update
	 * @param request property data to update
	 */
	void updateProperty(Integer id, PropertyRequest request);
}
