package org.ibp.api.java.ontology;

import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.ScaleSummary;

import java.util.List;

public interface OntologyScaleService {

	/**
	 * get all scales with details
	 *
	 * @return list of scales
	 */
	List<ScaleSummary> getAllScales();

	/**
	 * get scale using given id
	 *
	 * @param id scale id
	 * @return scale that matches id
	 */
	ScaleDetails getScaleById(String id);

	/**
	 * Adding new scale
	 *
	 * @param scaleSummary ScaleSummary
	 */
	GenericResponse addScale(ScaleSummary scaleSummary);

	/**
	 * update scale with new request data
	 * @param id scale to update
	 * @param scaleSummary ScaleSummary instance that have new data
	 */
	void updateScale(String id, ScaleSummary scaleSummary);

	/**
	 * Delete a scale using given id
	 *
	 * @param id scale to be deleted
	 */
	void deleteScale(String id);
}
