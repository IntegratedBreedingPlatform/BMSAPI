package org.ibp.api.java.design;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.rest.design.ExperimentDesignInput;

import java.util.List;

public interface ExperimentDesignService {

	void generateAndSaveDesign(String cropName, int studyId, ExperimentDesignInput experimentDesignInput);

	void deleteDesign(int studyId);

	List<ExperimentDesignType> getExperimentalDesignTypes();

}
