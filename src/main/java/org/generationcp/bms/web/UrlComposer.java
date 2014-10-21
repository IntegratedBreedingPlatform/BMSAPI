package org.generationcp.bms.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UrlComposer {

	@Autowired
	private HttpServletRequest httpServletRequest;
	
	public String getBaseUrl() {
		if(httpServletRequest == null) {
			return "";
		}		
		return String.format("%s://%s:%s", httpServletRequest.getScheme(), httpServletRequest.getServerName(), httpServletRequest.getServerPort());	
	}
	
	public String getStudySummaryUrl(Integer studyId) {
		return String.format("%s/study/%s", getBaseUrl(), studyId);
	}
	
	public String getStudyDetailsUrl(Integer studyId) {
		return String.format("%s/study/%s/details", getBaseUrl(), studyId);
	}
	
	public String getDataSetDetailsUrl(Integer dataSetId) {
		return String.format("%s/study/dataset/%s", getBaseUrl(), dataSetId);
	}
	
	public String getObservationDetailsUrl(Integer studyId, Integer traitId) {
		return String.format("%s/study/%s/trait/%s", getBaseUrl(), studyId, traitId);
	}
	
	public String getListDetailsUrl(Integer germplasmListId) {
		return String.format("%s/germplasm/list/%s", getBaseUrl(), germplasmListId);
	}
	
}
