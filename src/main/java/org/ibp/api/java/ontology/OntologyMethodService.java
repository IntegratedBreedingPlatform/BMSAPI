package org.ibp.api.java.ontology;

import java.util.List;

import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.MethodResponse;
import org.ibp.api.domain.ontology.MethodSummary;

public interface OntologyMethodService {

	/**
	 * Get all Methods
	 *
	 * @return List of all Methods
	 */
	List<MethodSummary> getAllMethods();

	/**
	 * Get Method by id
	 *
	 * @param id of the Method to retrieve
	 * @return Method details
	 */
	MethodResponse getMethod(String id);

	/**
	 * Add new Method
	 *
	 * @param method Method to add
	 * @return Newly created method id
	 */
	GenericResponse addMethod(MethodSummary method);

	/**
	 * Update Method
	 *
	 * @param id of the Method to be updated
	 * @param Method data to be updated
	 */
	void updateMethod(String id, MethodSummary method);

	/**
	 * Delete Method
	 *
	 * @param id of the Method to be deleted
	 */
	void deleteMethod(String id);

}
