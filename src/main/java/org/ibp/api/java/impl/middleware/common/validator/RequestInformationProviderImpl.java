package org.ibp.api.java.impl.middleware.common.validator;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class RequestInformationProviderImpl implements RequestInformationProvider {

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getUrlTemplateAttributes() {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return(Map<String, String>) requestAttributes.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
	}

}
