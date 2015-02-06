package org.generationcp.bms.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

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
	
	public String getStudyDetailsUrl(Integer studyId) {
		return String.format("%s/study/%s", getBaseUrl(), studyId);
	}
	
	public String getDataSetDetailsUrl(Integer dataSetId) {
		return String.format("%s/study/dataset/%s", getBaseUrl(), dataSetId);
	}
	
	public String getObservationDetailsUrl(Integer studyId) {
		return String.format("%s/study/observations", getBaseUrl());
	}
	
	public String getListDetailsUrl(Integer germplasmListId) {
		return String.format("%s/germplasm/list/%s", getBaseUrl(), germplasmListId);
	}
	
	public String getVariableDetailsUrl(Integer variableId) {
		return String.format("%s/ontology/var/%s", getBaseUrl(), variableId);
	}
	
}
