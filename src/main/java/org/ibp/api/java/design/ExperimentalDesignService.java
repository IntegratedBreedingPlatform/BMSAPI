package org.ibp.api.java.design;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.rest.design.ExperimentalDesignInput;

import java.util.List;
import java.util.Optional;

public interface ExperimentalDesignService {

	void generateAndSaveDesign(String cropName, int studyId, ExperimentalDesignInput experimentalDesignInput);

	void deleteDesign(int studyId);

	List<ExperimentDesignType> getExperimentalDesignTypes();

	Optional<Integer> getStudyExperimentalDesignTypeTermId(int studyId);

}
