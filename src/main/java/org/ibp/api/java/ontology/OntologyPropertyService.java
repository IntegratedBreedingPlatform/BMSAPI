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
	 * @throws MiddlewareException
	 */
	List<PropertySummary> getAllProperties();

	/**
	 * get property by given property id
	 *
	 * @param id
	 *            property id
	 * @return property that have given id
	 * @throws MiddlewareException
	 */
	PropertyResponse getProperty(Integer id);

	/**
	 * add property using given input data
	 *
	 * @param request
	 *            property data to be added
	 * @return newly created property id
	 * @throws MiddlewareException
	 */
	GenericResponse addProperty(PropertyRequest request);

	/**
	 * get all properties containing class name
	 *
	 * @param propertyClass
	 *            class name to be search in property
	 * @return list of properties
	 * @throws MiddlewareException
	 */
	List<PropertySummary> getAllPropertiesByClass(String propertyClass);

	/**
	 * delete property if not used
	 *
	 * @param id
	 *            property to be deleted
	 * @return if property deleted or not
	 * @throws MiddlewareException
	 */
	boolean deleteProperty(Integer id);

	/**
	 *
	 * @param id
	 *            property to update
	 * @param request
	 *            property data to update
	 * @throws MiddlewareException
	 */
	void updateProperty(Integer id, PropertyRequest request);
}
