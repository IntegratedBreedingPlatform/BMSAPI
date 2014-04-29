package org.generationcp.bms.util;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.StandardVariableSummary;
import org.generationcp.middleware.domain.oms.TermSummary;

public class Mapper {
	
	public StandardVariableSummary extractSummary(StandardVariable details) {
		if(details == null) {
			return null;
		}
		
		StandardVariableSummary summary = new StandardVariableSummary(details.getId(), details.getName(), details.getDescription());
		
		summary.setProperty(new TermSummary(details.getProperty().getId(), details.getProperty().getName(), details.getProperty().getDefinition()));
		summary.setMethod(new TermSummary(details.getMethod().getId(), details.getMethod().getName(), details.getMethod().getDefinition()));
		summary.setScale(new TermSummary(details.getScale().getId(), details.getScale().getName(), details.getScale().getDefinition()));
		summary.setIsA(new TermSummary(details.getIsA().getId(), details.getIsA().getName(), details.getIsA().getDefinition()));
		summary.setStoredIn(new TermSummary(details.getStoredIn().getId(), details.getStoredIn().getName(), details.getStoredIn().getDefinition()));
		summary.setDataType(new TermSummary(details.getDataType().getId(), details.getDataType().getName(), details.getDataType().getDefinition()));
		summary.setPhenotypicType(details.getPhenotypicType());

		return summary;
	}

}
