package org.ibp.api.java.design;

import org.ibp.api.exception.BVDesignException;
import org.ibp.api.rest.design.ExperimentDesignInput;

public interface ExperimentDesignService {

	void generateAndSaveDesign(int studyId, ExperimentDesignInput experimentDesignInput) throws BVDesignException;

}
