package org.ibp.api.java.impl.middleware.study.validator;

import org.generationcp.middleware.api.study.AdvanceStudyRequest;
import org.springframework.stereotype.Component;

// TODO: implement it
@Component
public class AdvanceValidator {

	public void addValidations(final AdvanceStudyRequest request) {
		// TODO: DEFINE ALL REQUIRED VALIDATIONS!!!!!!
		// TODO: validate if there are lines with BM_CODE_VTE values if it's selected to defines a breeding method for each line.
		//  Currently, this validation it's been doing in advance/study/countPlots/{variableId}
		// TODO: validate breeding method is not generative
		// TODO: what happens if BM_CODE_VTE is used and there are values corresponding to Generative methods?? Currently the BM_CODE_VTE
		//  are not filtering only by MAN and DER
		// TODO: validate there is at least one instance selected
		// TODO: validate that AdvanceStudyDTO::instanceIds corresponds to dataset
		// TODO: validate request.getSelectionTraitRequest()
	}

}
