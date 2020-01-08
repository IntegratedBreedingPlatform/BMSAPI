
package org.ibp.api.java.impl.middleware.common;

import liquibase.util.StringUtils;
import org.generationcp.middleware.ContextHolder;
import org.ibp.api.java.crop.CropService;
import org.ibp.api.java.program.ProgramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Component
public class ContextResolverImpl implements ContextResolver {

	private static final Logger LOG = LoggerFactory.getLogger(ContextResolverImpl.class);
	public static final String BRAPI = "brapi";

	@Autowired
	private CropService cropService;

	@Autowired
	private ProgramService programService;

	@Override
	public String resolveDatabaseFromUrl() throws ContextResolutionException {
		return String.format(Constants.DB_NAME_FORMAT, resolveCropNameFromUrl());
	}


	@Override
	public String resolveCropNameFromUrl() throws ContextResolutionException {
		final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		if (request == null) {
			throw new ContextResolutionException("Request is null");
		}

		final String path = request.getRequestURI().substring(request.getContextPath().length());
		ContextResolverImpl.LOG.debug("Request path: " + path);
		final String[] parts = path.trim().toLowerCase().split("/");


		String cropName = "";
		boolean instanceLevelAPI = true;
		if (parts.length >= 3) {
			final boolean isBrApiURI = Arrays.stream(parts).anyMatch(BRAPI::equals);
			if (isBrApiURI) {
				// Exclude instance-level BrAPI calls (eg. /bmsapi/brapi/v1/crops) in crop resolution
				// BrAPI calls put crop name as first path parameter after context path e.g. /bmsapi/maize/brapi/v1/locations
				instanceLevelAPI = BRAPI.equals(parts[1]);
				cropName = instanceLevelAPI? "" : parts[1];

			} else if ("crops".equals(parts[1])){
				// internal BMSAPI crop/program services start with "crops" (eg. /bmsapi/crops/maize/locations
				cropName = parts[2];
				instanceLevelAPI = false;
			}

			if (!StringUtils.isEmpty(cropName)) {
				final List<String> installedCrops = this.cropService.getInstalledCrops();
				if (!installedCrops.contains(cropName)) {
					throw new ContextResolutionException("Invalid crop " + cropName + " for URL:" + path);
				}
				ContextHolder.setCurrentCrop(cropName);
			}
		}

		if (!instanceLevelAPI && StringUtils.isEmpty(cropName)) {
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

		final String path = request.getRequestURI().substring(request.getContextPath().length());
		final String[] parts = path.trim().toLowerCase().split("/");
		final int programsTokenIndex = Arrays.asList(parts).indexOf("programs");
		String programUUID = "";
		// IF the URL contains "programs" token, resolve the program UUID as the next token (eg. crops/maize/programs/abc-123/studies would yield abc-123 as the program UUID)
		if (programsTokenIndex != -1 && (programsTokenIndex<parts.length-1)) {
			programUUID = parts[programsTokenIndex+1];

		// If not found in URL path, search in request parameters for "programUUID"
		} else {
			final String programUuidRequestParam = request.getParameter("programUUID");
			if (!StringUtils.isEmpty(programUuidRequestParam)) {
				programUUID =  programUuidRequestParam;
			}
		}

		// If program UUID was supplied, verify that it is valid for given crop (if specified too)
		// It is possible for crop to not have been supplied, eg. for instance-level resources like GET /users
		if (!StringUtils.isEmpty(programUUID)) {
			final String crop = this.resolveCropNameFromUrl();
			if (!StringUtils.isEmpty(crop) && programService.getByUUIDAndCrop(crop,programUUID) == null) {
				throw new ContextResolutionException("Invalid program: " + programUUID + " for crop: " + crop + " for service with path " + path);
			}
			ContextHolder.setCurrentProgram(programUUID);

		} else {
			ContextHolder.setCurrentProgram(null);
		}


		return programUUID;

	}
}
