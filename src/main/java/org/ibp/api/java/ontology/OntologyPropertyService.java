package org.ibp.api.java.ontology;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
	 * @throws MiddlewareQueryException
	 */
	List<PropertySummary> getAllProperties() throws MiddlewareQueryException;

	/**
	 * get property by given property id
	 *
	 * @param id
	 *            property id
	 * @return property that have given id
	 * @throws MiddlewareQueryException
	 */
	PropertyResponse getProperty(Integer id) throws MiddlewareException;

	/**
	 * add property using given input data
	 *
	 * @param request
	 *            property data to be added
	 * @return newly created property id
	 * @throws MiddlewareQueryException
	 */
	GenericResponse addProperty(PropertyRequest request) throws MiddlewareException;

	/**
	 * get all properties containing class name
	 *
	 * @param propertyClass
	 *            class name to be search in property
	 * @return list of properties
	 * @throws MiddlewareQueryException
	 */
	List<PropertySummary> getAllPropertiesByClass(String propertyClass)
			throws MiddlewareQueryException;

	/**
	 * delete property if not used
	 *
	 * @param id
	 *            property to be deleted
	 * @return if property deleted or not
	 * @throws MiddlewareQueryException
	 */
	boolean deleteProperty(Integer id) throws MiddlewareException;

	/**
	 *
	 * @param id
	 *            property to update
	 * @param request
	 *            property data to update
	 * @throws MiddlewareQueryException
	 * @throws MiddlewareException
	 */
	void updateProperty(Integer id, PropertyRequest request) throws MiddlewareException;
}
