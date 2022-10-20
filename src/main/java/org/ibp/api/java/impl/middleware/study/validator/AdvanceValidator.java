package org.ibp.api.java.impl.middleware.study.validator;

import org.generationcp.middleware.api.study.AdvanceStudyRequest;
import org.springframework.stereotype.Component;

// TODO: implement it
@Component
public class AdvanceValidator {

	public void addValidations(final AdvanceStudyRequest request) {
		// TODO: DEFINE ALL REQUIRED VALIDATIONS!!!!!!
		// TODO: check for current validation in advance/study/countPlots/{variableId}

		// TODO: validate experiment was already generated

		// TODO: validate instances
		//  validate at least one instance is selected
		//  validate instance corresponds to the study

		// TODO: validate breedingMethodSelectionRequest
		//  validate breedingMethodId or methodVariateId is selected
		//  	if breedingMethodId was selected
		//  		-> validate is a valid one
		//			-> validate is not a generative BM
		//		if methodVariateId was selected -> validate that plotdataset have it
		// TODO: validate "BULKS" (from old advance) section
		// 		-> if a specific bulking breeding method is selected, then breeding BreedingMethodRequest::allPlotSelect
		// 		or BreedingMethodRequest::plotVariateId must be set
		//			-> if  plotVariateId is selected then validate if it corresponds to the plot dataset

		// TODO: validate request.getSelectionTraitRequest()
		//  	-> validate given dataset corresponds to the study
		//		-> validate given variableId corresponds to the given dataset
	}

}
