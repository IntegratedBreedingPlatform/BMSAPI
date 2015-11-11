
package org.ibp.api.java.ontology;

import java.util.List;

import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.MethodDetails;

public interface MethodService {

	/**
	 * Get all Methods
	 *
	 * @return List of all Methods
	 */
	List<MethodDetails> getAllMethods();

	/**
	 * Get Method by id
	 *
	 * @param id of the Method to retrieve
	 * @return Method details
	 */
	MethodDetails getMethod(String id);

	/**
	 * Add new Method
	 *
	 * @param method Method to add
	 * @return Newly created method id
	 */
	GenericResponse addMethod(MethodDetails method);

	/**
	 * Update Method
	 *
	 * @param id of the Method to be updated
	 * @param method data to be updated
	 */
	void updateMethod(String id, MethodDetails method);

	/**
	 * Delete Method
	 *
	 * @param id of the Method to be deleted
	 */
	void deleteMethod(String id);

}
