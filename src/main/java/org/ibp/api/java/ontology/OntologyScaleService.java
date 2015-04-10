package org.ibp.api.java.ontology;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.ScaleRequest;
import org.ibp.api.domain.ontology.ScaleResponse;
import org.ibp.api.domain.ontology.ScaleSummary;

import java.util.List;

public interface OntologyScaleService {

	/**
	 * get all scales with details
	 *
	 * @return list of scales
	 * @throws MiddlewareQueryException
	 */
	List<ScaleSummary> getAllScales() throws MiddlewareQueryException;

	/**
	 * get scale using given id
	 *
	 * @param id
	 *            scale id
	 * @return scale that matches id
	 * @throws MiddlewareQueryException
	 */
	ScaleResponse getScaleById(Integer id) throws MiddlewareQueryException;

	/**
	 * Adding new scale
	 *
	 * @param request
	 *            ScaleRequest
	 * @throws MiddlewareQueryException
	 * @throws MiddlewareException
	 */
	GenericResponse addScale(ScaleRequest request) throws MiddlewareQueryException,
			MiddlewareException;

	/**
	 * update scale with new request data
	 *
	 * @param request
	 *            ScaleRequest instance that have new data
	 * @throws MiddlewareQueryException
	 * @throws MiddlewareException
	 */
	void updateScale(ScaleRequest request) throws MiddlewareQueryException, MiddlewareException;

	/**
	 * Delete a scale using given id
	 *
	 * @param id
	 *            scale to be deleted
	 * @throws MiddlewareQueryException
	 * @throws MiddlewareException
	 */
	void deleteScale(Integer id) throws MiddlewareQueryException, MiddlewareException;
}
