package org.ibp.api.java.impl.middleware.permission.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.observationunits.ObservationUnitService;
import org.ibp.api.java.permission.PermissionService;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BrapiPermissionValidator {

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ProgramService programService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyService studyService;

	@Autowired
	private ObservationUnitService observationUnitService;

	/**
	 * Used by brapi with no program ID param and at least crop level permission is required
	 *
	 * @param cropName
	 */
	public void validateUserHasAtLeastCropRoles(String cropName) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

		if (user.hasOnlyProgramRoles(cropName)) {
			throw new AccessDeniedException("");
		}
	}

	public List<String> validateProgramByProgramDbIds(final String cropName, final List<String> programDbIds,
		final boolean errorWhenInvalidIdExists) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

		if (user.hasOnlyProgramRoles(cropName)) {
			final List<String> validPrograms = this.getAllValidProgramsForUser(
				cropName, programDbIds, user.getUserid(), errorWhenInvalidIdExists);
			if (validPrograms != null)
				return validPrograms;
		}

		return programDbIds;
	}

	public List<String> validateProgramByProgramDbId(final String cropName, final String programDbId) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

		if (user.hasOnlyProgramRoles(cropName)) {
			final List<String> validPrograms = this.getAllValidProgramsForUser(cropName,
				Arrays.asList(programDbId), user.getUserid(), true);
			if (validPrograms != null)
				return validPrograms;
		} else if (StringUtils.isNotEmpty(programDbId)) {
			return Arrays.asList(programDbId);
		}

		return Collections.emptyList();
	}

	private List<String> getAllValidProgramsForUser(final String cropName, final List<String> programDbIds, final Integer userId,
		final boolean errorWhenInvalidIdExists) {
		final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
		programSearchRequest.setLoggedInUserId(userId);
		programSearchRequest.setCommonCropName(cropName);

		final List<String> userPrograms = this.programService.getFilteredPrograms(null, programSearchRequest)
			.stream().map(ProgramDTO::getUniqueID).collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(userPrograms)) {
			if(CollectionUtils.isNotEmpty(programDbIds) && !userPrograms.containsAll(programDbIds)) {
				if(errorWhenInvalidIdExists) {
					throw new AccessDeniedException("");
				}
				final List<String> programDbIdsArray = new ArrayList<>(programDbIds);
				programDbIdsArray.retainAll(userPrograms);
				if(CollectionUtils.isNotEmpty(programDbIdsArray))
					return programDbIdsArray;
			}

			return userPrograms;
		}
		return null;
	}

	public List<String> validateProgramByStudyDbId(final String cropName, final String studyDbId) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
		if (user.hasOnlyProgramRoles(cropName)) {
			if (StringUtils.isEmpty(studyDbId)) {
				throw new AccessDeniedException("");
			}

			final Integer trialDbId = this.studyDataManager.getProjectIdByStudyDbId(Integer.valueOf(studyDbId));

			if (trialDbId != null) {
				final DmsProject dmsProject = this.studyService.getDmSProjectByStudyId(trialDbId);

				if (dmsProject != null) {
					return this.getAllValidProgramsForUser(cropName, Arrays.asList(dmsProject.getProgramUUID()),
						user.getUserid(), true);
				}
			}
		}
		return Collections.emptyList();
	}

	public List<String> validateProgramByObservationUnitDbId(final String cropName, final String observationUnitDbId) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
		if (user.hasOnlyProgramRoles(cropName)) {
			if (StringUtils.isEmpty(observationUnitDbId)) {
				throw new AccessDeniedException("");
			}

			final ObservationUnitSearchRequestDTO obsRequestDto = new ObservationUnitSearchRequestDTO();
			obsRequestDto.setObservationUnitDbIds(Arrays.asList(observationUnitDbId));
			final List<ObservationUnitDto> observationList = this.observationUnitService
				.searchObservationUnits(null, null, obsRequestDto);
			final Set<String> programs = observationList.stream()
				.filter(obs -> StringUtils.isNotEmpty(obs.getProgramDbId()))
				.map(ObservationUnitDto::getProgramDbId).collect(Collectors.toSet());
			if (CollectionUtils.isNotEmpty(programs)) {
				return this.getAllValidProgramsForUser(cropName,
					new ArrayList<>(programs),
					user.getUserid(), true);
			}
		}
		return Collections.emptyList();
	}
}
