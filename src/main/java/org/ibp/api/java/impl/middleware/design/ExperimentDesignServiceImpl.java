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

		// TODO get List<ImportedGermplasm> germplasmList

		// TODO get programUUID of study

		// TODO determine applicable ExperimentDesignTypeService and call generateDesign

		// TODO Call MW save observation unit rows
	}
}
