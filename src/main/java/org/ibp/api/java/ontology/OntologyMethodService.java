package org.ibp.api.java.ontology;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
	 * @throws MiddlewareQueryException
	 */
	List<MethodSummary> getAllMethods() throws MiddlewareQueryException;

	/**
	 * get method by method id
	 *
	 * @param id
	 *            the method id
	 * @return method
	 * @throws MiddlewareQueryException
	 */
	MethodResponse getMethod(Integer id) throws MiddlewareQueryException;

	/**
	 * add method using given input data
	 *
	 * @param method
	 *            method to add
	 * @return newly created method id
	 * @throws MiddlewareQueryException
	 */
	GenericResponse addMethod(MethodRequest method) throws MiddlewareQueryException;

	/**
	 * update method data using given method id
	 *
	 * @param id
	 *            method to be updated
	 * @param request
	 *            method data to be updated
	 * @throws MiddlewareQueryException
	 *             , MiddlewareException
	 */
	void updateMethod(Integer id, MethodRequest request) throws MiddlewareQueryException, MiddlewareException;

	/**
	 * delete method using given id
	 *
	 * @param id
	 *            method to be deleted
	 * @throws MiddlewareQueryException
	 */
	void deleteMethod(Integer id) throws MiddlewareQueryException;

}
