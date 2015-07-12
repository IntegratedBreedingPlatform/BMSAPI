package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.study.FieldPlot;


public class StudyFieldMapUtility {

	private StudyFieldMapUtility() {
		
	}
	
	static FieldPlot[][] getDefaultPlots(final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo) {
		final int columns = fieldMapTrialInstanceInfo.getRowsInBlock() / fieldMapTrialInstanceInfo.getRowsPerPlot();
		
		final Integer rangesInBlock = fieldMapTrialInstanceInfo.getRangesInBlock();
		final FieldPlot[][] fieldPlots = new FieldPlot[columns][rangesInBlock];
		
		for (int i = 0; i < fieldPlots.length; i++) {
			FieldPlot[] fieldPlots2 = fieldPlots[i];
			for (int j = 0; j < fieldPlots2.length; j++) {
				fieldPlots[i][j] = new FieldPlot();
			}
		}
		return fieldPlots;
		
	}
	
	static CrossExpansionProperties getCrossExpansionProperties() {
		final CrossExpansionProperties crossExpansionProperties = new CrossExpansionProperties();
		crossExpansionProperties.setDefaultLevel(1);
		crossExpansionProperties.setWheatLevel(1);
		return crossExpansionProperties;
	}
}
