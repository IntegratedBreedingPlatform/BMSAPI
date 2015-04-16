package org.ibp.api.java.ontology;

import org.generationcp.middleware.exceptions.MiddlewareException;
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
	 * @throws MiddlewareException
	 */
	List<ScaleSummary> getAllScales() throws MiddlewareException;

	/**
	 * get scale using given id
	 *
	 * @param id
	 *            scale id
	 * @return scale that matches id
	 * @throws MiddlewareException
	 */
	ScaleResponse getScaleById(Integer id) throws MiddlewareException;

	/**
	 * Adding new scale
	 *
	 * @param request
	 *            ScaleRequest
	 * @throws MiddlewareException
	 */
	GenericResponse addScale(ScaleRequest request) throws MiddlewareException;

	/**
	 * update scale with new request data
	 *
	 * @param request
	 *            ScaleRequest instance that have new data
	 * @throws MiddlewareException
	 */
	void updateScale(ScaleRequest request) throws MiddlewareException;

	/**
	 * Delete a scale using given id
	 *
	 * @param id
	 *            scale to be deleted
	 * @throws MiddlewareException
	 */
	void deleteScale(Integer id) throws MiddlewareException;
}
