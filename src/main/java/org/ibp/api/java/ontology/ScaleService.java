
package org.ibp.api.java.ontology;

import java.util.List;

import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.ScaleDetails;

public interface ScaleService {

	/**
	 * get all scales with details
	 *
	 * @return list of scales
	 */
	List<ScaleDetails> getAllScales();

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
	 * @param scaleDetails ScaleDetails
	 */
	GenericResponse addScale(ScaleDetails scaleDetails);

	/**
	 * update scale with new request data
	 * 
	 * @param id scale to update
	 * @param scaleDetails ScaleDetails instance that have new data
	 */
	void updateScale(String id, ScaleDetails scaleDetails);

	/**
	 * Delete a scale using given id
	 *
	 * @param id scale to be deleted
	 */
	void deleteScale(String id);
}
