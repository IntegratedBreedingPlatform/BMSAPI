
package org.ibp.api.java.impl.middleware.common;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.middleware.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Map;

@Component
public class ContextResolverImpl implements ContextResolver {

	private static final Logger LOG = LoggerFactory.getLogger(ContextResolverImpl.class);

	@Override
	public String resolveDatabaseFromUrl() throws ContextResolutionException {
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
		ContextResolverImpl.LOG.debug("Crop Name: " + parts[2]);
		ContextHolder.setCurrentCrop(parts[2]);
		return String.format(Constants.DB_NAME_FORMAT, parts[2]);
	}
	
	@Override
	public String resolveCropName() {
		
		final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		final Map variableMap = (Map) request.getAttribute("org.springframework.web.servlet.HandlerMapping.uriTemplateVariables");
		
		if(variableMap != null) {
			return (String) variableMap.get("cropname");
		}
		
		return null;

	}
}
