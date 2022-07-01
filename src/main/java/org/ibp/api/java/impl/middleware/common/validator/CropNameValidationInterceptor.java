
package org.ibp.api.java.impl.middleware.common.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.middleware.api.crop.CropService;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.java.impl.middleware.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class CropNameValidationInterceptor extends HandlerInterceptorAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(CropNameValidationInterceptor.class);

	@Autowired
	private CropService cropServiceMW;

	@Autowired
	private RequestInformationProvider requestInformationProvider;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		final Map<String, String> uriTemplateVars = this.requestInformationProvider.getUrlTemplateAttributes();

		if (uriTemplateVars.containsKey(Constants.CROPNAME_URI_PARAM)) {
			String cropName = uriTemplateVars.get(Constants.CROPNAME_URI_PARAM);
			ErrorResponse errorResponse = null;
			try {
				CropType cropType = this.cropServiceMW.getCropTypeByName(cropName);
				if (cropType == null) {
					errorResponse = new ErrorResponse("error", "Invalid crop name path parameter: " + cropName);
				}
			} catch (MiddlewareException e) {
				String errorMessage = "Error while validating crop name path parameter";
				CropNameValidationInterceptor.LOG.error(errorMessage, e);
				errorResponse = new ErrorResponse("error", errorMessage + ": " + e.getMessage());
			}

			if (errorResponse != null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.setContentType("application/json");
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(response.getOutputStream(), errorResponse);
				return false; // abort handling
			}
		}
		return true; // continue handling
	}

	class ErrorResponse {

		private String status;
		private String message;

		public ErrorResponse(String status, String message) {
			this.status = status;
			this.message = message;
		}

		public String getStatus() {
			return this.status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getMessage() {
			return this.message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	protected void setCropServiceMW(CropService cropServiceMW) {
		this.cropServiceMW = cropServiceMW;
	}

	protected void setRequestInformationProvider(RequestInformationProvider requestInformationProvider) {
		this.requestInformationProvider = requestInformationProvider;
	}
}
