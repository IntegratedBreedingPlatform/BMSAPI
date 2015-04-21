package org.ibp.api.java.ontology;

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
	List<ScaleSummary> getAllScales();

	/**
	 * get scale using given id
	 *
	 * @param id
	 *            scale id
	 * @return scale that matches id
	 * @throws MiddlewareException
	 */
	ScaleResponse getScaleById(Integer id);

	/**
	 * Adding new scale
	 *
	 * @param request
	 *            ScaleRequest
	 * @throws MiddlewareException
	 */
	GenericResponse addScale(ScaleRequest request);

	/**
	 * update scale with new request data
	 *
	 * @param request
	 *            ScaleRequest instance that have new data
	 * @throws MiddlewareException
	 */
	void updateScale(ScaleRequest request);

	/**
	 * Delete a scale using given id
	 *
	 * @param id
	 *            scale to be deleted
	 * @throws MiddlewareException
	 */
	void deleteScale(Integer id);
}
