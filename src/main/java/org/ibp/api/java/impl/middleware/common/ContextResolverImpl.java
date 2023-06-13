
package org.ibp.api.java.impl.middleware.common;

import liquibase.util.StringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.ibp.api.java.crop.CropService;
import org.ibp.api.java.observationunits.ObservationUnitService;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.java.study.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContextResolverImpl implements ContextResolver {

	private static final Logger LOG = LoggerFactory.getLogger(ContextResolverImpl.class);
	public static final String BRAPI = "brapi";

	@Autowired
	private CropService cropService;

	@Autowired
	@Lazy
	private ProgramService programService;

	@Autowired
	private StudyService studyService;

	@Autowired
	private ObservationUnitService observationUnitService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Override
	public String resolveDatabaseFromUrl() throws ContextResolutionException {
		final String crop = this.resolveCropNameFromUrl();
		if (StringUtils.isEmpty(crop)) {
			throw new ContextResolutionException("Could not resolve database because crop name was not found");
		}
		return String.format(Constants.DB_NAME_FORMAT, crop);
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
		final boolean isBrApiURI = Arrays.stream(parts).anyMatch(BRAPI::equals);
		if (isBrApiURI) {
			// Exclude instance-level BrAPI calls (eg. /bmsapi/brapi/v1/crops) in crop resolution
			// BrAPI calls put crop name as first path parameter after context path e.g. /bmsapi/maize/brapi/v1/locations
			instanceLevelAPI = BRAPI.equals(parts[1]);
			cropName = instanceLevelAPI ? "" : parts[1];

		} else if ("crops".equals(parts[1])) {
			// internal BMSAPI crop/program services start with "crops" (eg. /bmsapi/crops/maize/locations
			cropName = parts[2];
			instanceLevelAPI = false;

			// If not found in URL path, search in request parameters for "cropName"
		} else {
			final String cropNameRequestParam = request.getParameter("cropName");
			if (!StringUtils.isEmpty(cropNameRequestParam)) {
				cropName = cropNameRequestParam;
			}
		}

		if (!StringUtils.isEmpty(cropName)) {
			final List<String> installedCrops = this.cropService.getInstalledCrops();
			if (!installedCrops.stream().map(crop -> crop.toUpperCase()).collect(Collectors.toList()).contains(cropName.toUpperCase())) {
				throw new ContextResolutionException("Invalid crop " + cropName + " for URL:" + path);
			}
			ContextHolder.setCurrentCrop(cropName);
		}

		if (!instanceLevelAPI && StringUtils.isEmpty(cropName)) {
			throw new ContextResolutionException("Could not resolve crop for URL:" + path);
		}
		ContextResolverImpl.LOG.debug("Crop Name: " + cropName);
		return cropName;
	}

	/**
	 * This need to be post construct because the study service is not available on object creation.
	 *
	 * @throws Exception
	 */
	@Override
	public String resolveProgramUuidFromRequest() throws ContextResolutionException {
		final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		if (request == null) {
			throw new ContextResolutionException("Request is null");
		}

		final String path = request.getRequestURI().substring(request.getContextPath().length());
		final String[] parts = path.trim().toLowerCase().split("/");
		final List partList = Arrays.asList(parts);

		final int programsTokenIndex = partList.indexOf("programs");
		String programUUID = "";
		// IF the URL contains "programs" token, resolve the program UUID as the next token (eg. crops/maize/programs/abc-123/studies would yield abc-123 as the program UUID)
		if (programsTokenIndex != -1 && (programsTokenIndex < parts.length - 1)) {
			programUUID = parts[programsTokenIndex + 1];

			// If not found in URL path, search in request parameters for "programUUID"
		} else {
			final String programUuidRequestParam = request.getParameter("programUUID");
			final int brapiTokenIndex = partList.indexOf("brapi");

			if (!StringUtils.isEmpty(programUuidRequestParam)) {
				programUUID = programUuidRequestParam;
			}

			// attempt to extract from other parameters in the URL if BRAPI
			else if (brapiTokenIndex != -1) {
				programUUID = this.extractProgramFromOtherParameters(request, partList);
			}
		}

		// If program UUID was supplied, verify that it is valid for given crop (if specified too)
		if (!StringUtils.isEmpty(programUUID)) {
			final String crop = this.resolveCropNameFromUrl();
			if (StringUtils.isEmpty(crop)) {
				throw new ContextResolutionException(
					"Could not resolve crop for program: " + programUUID + " for service with path " + path);
			}
			if (this.programService.getByUUIDAndCrop(crop, programUUID) == null) {
				throw new ContextResolutionException(
					"Invalid program: " + programUUID + " for crop: " + crop + " for service with path " + path);
			}
			ContextHolder.setCurrentProgram(programUUID);

		} else {
			ContextHolder.setCurrentProgram(null);
		}

		return programUUID;

	}

	private String extractProgramFromOtherParameters(final HttpServletRequest request, final List<String> parts) {
		final int studyTokenIndex = parts.indexOf("studies");
		final int trialTokenIndex = parts.indexOf("trials");

		Integer trialDbId = null;
		if (trialTokenIndex != -1 && (trialTokenIndex < parts.size() - 1)) {
			trialDbId = Integer.valueOf(parts.get(trialTokenIndex + 1));
		} else if (studyTokenIndex != -1 && (studyTokenIndex < parts.size() - 1)) {
			final String studyDbId = parts.get(studyTokenIndex + 1);
			trialDbId = this.studyDataManager.getProjectIdByStudyDbId(Integer.valueOf(studyDbId));
		}

		if (trialDbId != null) {
			final DmsProject dmsProject = this.studyService.getDmSProjectByStudyId(trialDbId);

			if (dmsProject != null) {
				return dmsProject.getProgramUUID();
			}
		}

		final int observationUnitIndex = Arrays.asList(parts).indexOf("observationunits");
		if (observationUnitIndex != -1 && (observationUnitIndex < parts.size() - 1)) {
			final String obsUnitId = parts.get(observationUnitIndex + 1);

			final ObservationUnitSearchRequestDTO obsRequestDto = new ObservationUnitSearchRequestDTO();
			obsRequestDto.setObservationUnitDbIds(Arrays.asList(obsUnitId));
			final List<ObservationUnitDto> observationList = this.observationUnitService
				.searchObservationUnits(null, null, obsRequestDto);
			if (!CollectionUtils.isEmpty(observationList)) {
				return observationList.get(0).getProgramDbId();
			}
		}

		return "";
	}

	void setCropService(final CropService cropService) {
		this.cropService = cropService;
	}

	void setProgramService(final ProgramService programService) {
		this.programService = programService;
	}
}
