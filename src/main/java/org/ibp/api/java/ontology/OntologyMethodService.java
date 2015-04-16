package org.ibp.api.java.ontology;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.MethodRequest;
import org.ibp.api.domain.ontology.MethodResponse;
import org.ibp.api.domain.ontology.MethodSummary;

import java.util.List;

public interface OntologyMethodService {

	/**
	 * get all methods using middleware ontology service
	 *
	 * @return list of methods
	 * @throws MiddlewareException
	 */
	List<MethodSummary> getAllMethods() throws MiddlewareException;

	/**
	 * get method by method id
	 *
	 * @param id
	 *            the method id
	 * @return method
	 * @throws MiddlewareException
	 */
	MethodResponse getMethod(Integer id) throws MiddlewareException;

	/**
	 * add method using given input data
	 *
	 * @param method
	 *            method to add
	 * @return newly created method id
	 * @throws MiddlewareException
	 */
	GenericResponse addMethod(MethodRequest method) throws MiddlewareException;

	/**
	 * update method data using given method id
	 *
	 * @param id
	 *            method to be updated
	 * @param request
	 *            method data to be updated
	 * @throws MiddlewareException
	 */
	void updateMethod(Integer id, MethodRequest request) throws MiddlewareException;

	/**
	 * delete method using given id
	 *
	 * @param id
	 *            method to be deleted
	 * @throws MiddlewareException
	 */
	void deleteMethod(Integer id) throws MiddlewareException;

}
