
package org.ibp.api.java.ontology;

import java.util.List;

import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.ontology.VariableType;

public interface ModelService {

	/**
	 * get all data types
	 *
	 * @return list of data types
	 */
	List<DataType> getAllDataTypes();

	/**
	 * Check provided data type weather it is equal to numeric or not.
	 * @param dataTypeId DataType Id
	 * @return true if data type is numerical
	 */
	boolean isNumericDataType(String dataTypeId);

	/**
	 * Check provided data type weather it is equal to categorical or not.
	 * @param dataTypeId DataType Id
	 * @return true if data type is Categorical
	 */
	boolean isCategoricalDataType(String dataTypeId);

	/**
	 * get all classes
	 *
	 * @return list of classes
	 */
	List<String> getAllClasses();

	/**
	 * Get List of all variable types
	 * @param Boolean – excludeRestrictedTypes
	 * @return List of Variable Types
	 */
	List<VariableType> getAllVariableTypes(Boolean excludeRestrictedTypes);
}
