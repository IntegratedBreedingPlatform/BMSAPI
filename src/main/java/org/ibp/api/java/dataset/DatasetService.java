package org.ibp.api.java.dataset;

import org.ibp.api.rest.dataset.DatasetDTO;

import java.util.List;
import java.util.Set;

public interface DatasetService {

	long countPhenotypes(final Integer studyId, final Integer datasetId, final List<Integer> traitIds);

	List<DatasetDTO> getDatasetByStudyId(final Integer studyId, final Set<Integer> filterByTypeIds);

}
