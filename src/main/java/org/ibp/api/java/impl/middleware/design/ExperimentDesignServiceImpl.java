package org.ibp.api.java.impl.middleware.design;

import org.ibp.api.java.design.ExperimentDesignService;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExperimentDesignServiceImpl implements ExperimentDesignService {

	@Override
	public void generateAndSaveDesign(final int studyId, final ExperimentDesignInput experimentDesignInput) {
		// This will be the main controller for generating and saving design

		// TODO 1. Get List<ImportedGermplasm> germplasmList
		//1.1 Move ImportedGermplasm, ListDataProjectUtil from Fielbook to Middleware
		//1.2 Create MW service to return List<ImportedGermplasm> for study germplasm list

		// TODO 2. Get programUUID of study

		// TODO 3. Determine applicable ExperimentDesignTypeService and call generateDesign

		// TODO 4. Call MW delete experiment design

		// TODO 5. Call MW save observation unit rows and variables
	}
}
