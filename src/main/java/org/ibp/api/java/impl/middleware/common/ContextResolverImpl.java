
package org.ibp.api.java.impl.middleware.common;

import javax.servlet.http.HttpServletRequest;

import liquibase.util.StringUtils;
import org.generationcp.middleware.ContextHolder;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.crop.CropService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Component
public class ContextResolverImpl implements ContextResolver {

	private static final Logger LOG = LoggerFactory.getLogger(ContextResolverImpl.class);

	@Autowired
	private CropService cropService;

	@Override
	public String resolveDatabaseFromUrl() throws ContextResolutionException {
		return String.format(Constants.DB_NAME_FORMAT, resolveCropNameFromUrl());
	}

	@Override
	public String resolveCropNameFromUrl() throws ContextResolutionException {
		return this.resolveCropNameFromUrl(true, true);
	}


	@Override
	public String resolveCropNameFromUrl(final boolean doRequireCrop, final boolean includeBrAPI) throws ContextResolutionException {
		final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		if (request == null) {
			throw new ContextResolutionException("Request is null");
		}

		String path = request.getRequestURI().substring(request.getContextPath().length());
		ContextResolverImpl.LOG.debug("Request path: " + path);
		String[] parts = path.trim().toLowerCase().split("/");
		if (parts.length < 3 && doRequireCrop) {
			ContextResolverImpl.LOG.error("BAD URL Request :" + path);
			throw new ContextResolutionException("URL too short:" + path, new Exception("Expecting crop name"));
		}

		String cropName = "";
		if (parts.length >= 3) {
			final boolean isBrApiURI = Arrays.stream(parts).anyMatch("brapi"::equals);
			if (includeBrAPI && isBrApiURI) {
				// BrAPI calls put crop name as first path parameter after context path e.g. /bmsapi/maize/brapi/v1/locations
				cropName = parts[1];

			} else if (!isBrApiURI){
				// internal BMSAPI calls put crop name as second path parameter after context path e.g. /bmsapi/locations/maize/list
				cropName = parts[2];
			}

			if (!StringUtils.isEmpty(cropName)) {
				final List<String> installedCrops = this.cropService.getInstalledCrops();
				if (!installedCrops.contains(cropName)) {
					throw new ContextResolutionException("Invalid crop " + cropName + " for URL:" + path);
				}
				ContextHolder.setCurrentCrop(cropName);
			}
		}

		if (doRequireCrop && StringUtils.isEmpty(cropName)) {
			throw new ContextResolutionException("Could not resolve crop for URL:" + path);
		}
		ContextResolverImpl.LOG.debug("Crop Name: " + cropName);
		return cropName;
	}

	@Override
	public String resolveProgramUuidFromRequest() throws ContextResolutionException {
		final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		if (request == null) {
			throw new ContextResolutionException("Request is null");
		}

		final String programUuid = request.getParameter("programUUID");
		if (!StringUtils.isEmpty(programUuid)) {
			// TODO Check if programUUID is valid and determine if we call ContextHolder.setCurrentProgram here
			return programUuid;
		}

		return "";

	}
}
