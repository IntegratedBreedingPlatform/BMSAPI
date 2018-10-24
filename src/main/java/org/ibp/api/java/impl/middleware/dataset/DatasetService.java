package org.ibp.api.java.impl.middleware.dataset;

import org.ibp.api.rest.dataset.DatasetGeneratorInput;

/**
 * Created by clarysabel on 10/24/18.
 */
public interface DatasetService {

		Integer generateSubObservationDataset(DatasetGeneratorInput datasetGeneratorInput);

}
