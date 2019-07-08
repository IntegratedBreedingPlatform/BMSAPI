package org.ibp.api.java.dataset;

import java.util.List;

public interface DatasetTypeService {
	
	List<String> getObservationLevels(final Integer pageSize, final Integer pageNumber);

	Long countObservationLevels();
}
