package org.ibp.api.domain.common;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


public class BmsRequestAttributeImpl implements BmsRequestAttributes {

	@Override
	public Map<?, ?> getRequestAttributes() {
		final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		return (Map<?, ?>) request.getAttribute("org.springframework.web.servlet.HandlerMapping.uriTemplateVariables");
	}

}
