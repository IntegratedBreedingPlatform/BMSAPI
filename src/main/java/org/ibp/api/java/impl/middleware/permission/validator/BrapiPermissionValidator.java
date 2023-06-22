package org.ibp.api.java.impl.middleware.permission.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.permission.PermissionService;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

	public void validatePermissions(final String cropName, final String... permissions) {
		final Integer userId = this.securityService.getCurrentlyLoggedInUser().getUserid();
		final List<String> userPermissions = this.permissionService.getPermissions(userId, cropName, null, true)
			.stream().map(PermissionDto::getName).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(userPermissions)
			&& !CollectionUtils.containsAny(userPermissions, Arrays.asList(permissions))) {
			throw new AccessDeniedException("");
		}
	}

	public List<String> validateProgramByProgramDbId(final String cropName, final String programDbId) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

		if (user.hasOnlyProgramRoles(cropName)) {
			final List<String> validPrograms = this.getAllValidProgramsForUser(cropName, programDbId, user.getUserid());
			if (validPrograms != null)
				return validPrograms;
		} else if (StringUtils.isNotEmpty(programDbId)) {
			return Arrays.asList(programDbId);
		}

		return Collections.EMPTY_LIST;
	}

	private List<String> getAllValidProgramsForUser(final String cropName, final String programDbId, final Integer userId) {
		final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
		programSearchRequest.setLoggedInUserId(userId);
		programSearchRequest.setCommonCropName(cropName);

		final List<String> userPrograms = this.programService.getFilteredPrograms(null, programSearchRequest)
			.stream().map(ProgramDTO::getUniqueID).collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(userPrograms)) {
			if (StringUtils.isNotEmpty(programDbId)) {
				if (!userPrograms.contains(programDbId)) {
					throw new AccessDeniedException("User is not authorized for the program.");
				}
				return Arrays.asList(programDbId);
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
					return this.getAllValidProgramsForUser(cropName, dmsProject.getProgramUUID(), user.getUserid());
				}
			}
		}
		return Collections.EMPTY_LIST;
	}
}
