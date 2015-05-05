
package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.domain.common.ValidationErrors;
import org.ibp.api.exception.ApiRequestValidationException2;
import org.ibp.api.java.impl.middleware.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class CropNameValidationInterceptor extends HandlerInterceptorAdapter {

	protected static final String INVALID_CROP_NAME = "selected.crop.not.valid";

	@Autowired
	private WorkbenchDataManager workbenchDataManager;
	
	@Autowired
	private RequestInformationProvider requestInformationProvider;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		final Map<String, String> uriTemplateVars = requestInformationProvider.getUrlTemplateAttributes();

		if (uriTemplateVars.containsKey(Constants.CROPNAME_URI_PARAM)) {
			String cropName = uriTemplateVars.get(Constants.CROPNAME_URI_PARAM);
			CropType cropType = workbenchDataManager.getCropTypeByName(cropName);
			if (cropType == null) {
				ValidationErrors validationErrors = new ValidationErrors();
				validationErrors.addError(INVALID_CROP_NAME);
				throw new ApiRequestValidationException2(validationErrors);
			}
		}
		return true; // continue handling
	}
	
	protected void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	
	protected void setRequestInformationProvider(RequestInformationProvider requestInformationProvider) {
		this.requestInformationProvider = requestInformationProvider;
	}
}
