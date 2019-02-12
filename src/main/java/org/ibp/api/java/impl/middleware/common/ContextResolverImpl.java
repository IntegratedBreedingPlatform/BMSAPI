
package org.ibp.api.java.impl.middleware.common;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.middleware.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ContextResolverImpl implements ContextResolver {

	private static final Logger LOG = LoggerFactory.getLogger(ContextResolverImpl.class);

	@Override
	public String resolveDatabaseFromUrl() throws ContextResolutionException {
		return String.format(Constants.DB_NAME_FORMAT, resolveCropNameFromUrl());
	}

	@Override
	public String resolveCropNameFromUrl() throws ContextResolutionException {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

		if (request == null) {
			throw new ContextResolutionException("Request is null");
		}

		String path = request.getRequestURI().substring(request.getContextPath().length());
		ContextResolverImpl.LOG.debug("Request path: " + path);
		String[] parts = path.trim().toLowerCase().split("/");
		if (parts.length < 3) {
			ContextResolverImpl.LOG.error("BAD URL Request :" + path);
			throw new ContextResolutionException("BAD URL:" + path, new Exception("Expecting crop name"));
		}

		String cropName = "";
		if ("brapi".equals(parts[2])) {
			// BrAPI calls put crop name as first path parameter after context path e.g. /bmsapi/maize/brapi/v1/locations
			cropName = parts[1];
		} else {
			// internal BMSAPI calls put crop name as second path parameter after context path e.g. /bmsapi/locations/maize/list
			cropName = parts[2];
		}
		ContextHolder.setCurrentCrop(cropName);
		ContextResolverImpl.LOG.debug("Crop Name: " + cropName);
		return cropName;
	}
}
