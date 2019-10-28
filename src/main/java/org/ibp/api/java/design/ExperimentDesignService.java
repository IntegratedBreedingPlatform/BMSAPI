package org.ibp.api.java.design;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.rest.design.ExperimentalDesignInput;

import java.util.List;

public interface ExperimentDesignService {

	void generateAndSaveDesign(String cropName, int studyId, ExperimentalDesignInput experimentalDesignInput);

	void deleteDesign(int studyId);

	List<ExperimentDesignType> getExperimentalDesignTypes();

}
