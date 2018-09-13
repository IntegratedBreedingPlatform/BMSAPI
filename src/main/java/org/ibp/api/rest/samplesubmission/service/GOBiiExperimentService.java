package org.ibp.api.rest.samplesubmission.service;

import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;
import org.ibp.api.rest.samplesubmission.domain.experiment.GOBiiExperiment;

/**
 * Created by clarysabel on 9/13/18.
 */
public interface GOBiiExperimentService {

	Integer postGOBiiExperiment(GOBiiToken goBiiToken, GOBiiExperiment goBiiExperiment);

}
