package org.generationcp.bms.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("unused")
@Component
public class ContextResolverImpl implements ContextResolver {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextResolverImpl.class);

    @Override
    public String resolveDatabaseFromUrl() throws ContextResolutionException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.debug("Request: " + request.getRequestURI());

        String requestURL = request.getRequestURI();
        int a = requestURL.indexOf("/");
        int b = requestURL.indexOf("/", a + 1);
        String dbName = requestURL.substring(b + 1, requestURL.indexOf("/", b + 1));
        return String.format("ibdbv2_%s_merged", dbName.trim().toLowerCase().replaceAll("\\s+", "_"));
    }
}
