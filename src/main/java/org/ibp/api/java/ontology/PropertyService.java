
package org.ibp.api.java.ontology;

import java.util.List;

import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.PropertyDetails;

public interface PropertyService {

	/**
	 * get all properties
	 *
	 * @return list of properties
	 */
	List<PropertyDetails> getAllProperties();

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
	GenericResponse addProperty(PropertyDetails request);

	/**
	 * get all properties containing class name
	 *
	 * @param propertyClass class name to be search in property
	 * @return list of properties
	 */
	List<PropertyDetails> getAllPropertiesByClass(String propertyClass);

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
	void updateProperty(String id, PropertyDetails request);
}
