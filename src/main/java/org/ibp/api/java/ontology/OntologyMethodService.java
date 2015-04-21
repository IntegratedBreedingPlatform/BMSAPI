package org.ibp.api.java.ontology;

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
	List<MethodSummary> getAllMethods();

	/**
	 * get method by method id
	 *
	 * @param id
	 *            the method id
	 * @return method
	 * @throws MiddlewareException
	 */
	MethodResponse getMethod(Integer id);

	/**
	 * add method using given input data
	 *
	 * @param method
	 *            method to add
	 * @return newly created method id
	 * @throws MiddlewareException
	 */
	GenericResponse addMethod(MethodRequest method);

	/**
	 * update method data using given method id
	 *
	 * @param id
	 *            method to be updated
	 * @param request
	 *            method data to be updated
	 * @throws MiddlewareException
	 */
	void updateMethod(Integer id, MethodRequest request);

	/**
	 * delete method using given id
	 *
	 * @param id
	 *            method to be deleted
	 * @throws MiddlewareException
	 */
	void deleteMethod(Integer id);

}
