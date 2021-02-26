
package org.ibp.api.java.impl.middleware.program;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.Util;
import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.program.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProgramServiceImpl implements ProgramService {

	@Autowired
	@Lazy
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private org.generationcp.middleware.api.program.ProgramService programService;

	@Autowired
	private UserService userService;

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public List<ProgramSummary> listProgramsByCropName(final String cropName) {
		try {
			return this.convertToProgramSummaries(this.workbenchDataManager.getProjectsByCropName(cropName));
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public List<ProgramSummary> listProgramsByCropNameAndUser(final ProgramSearchRequest programSearchRequest) {
		try {
			return this.convertToProgramSummaries(this.workbenchDataManager.getProjects(null, programSearchRequest ));
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public List<ProgramDTO> filterPrograms(final Pageable pageable, ProgramSearchRequest programSearchRequest) {
		return this.programService.filterPrograms(programSearchRequest, pageable);
	}

	@Override
	public List<ProgramDetailsDto> getProgramsByFilter(final Pageable pageable, final ProgramSearchRequest programSearchRequest) {
		this.validateProgramSearchRequest(programSearchRequest);
		final List<ProgramDetailsDto> programDetailsDtoList = new ArrayList<>();
		final List<Project> projectList = this.workbenchDataManager.getProjects(pageable, programSearchRequest);
		if (!projectList.isEmpty()) {
			for (final Project project : projectList) {
				final WorkbenchUser user = this.userService.getUserById(project.getUserId());
				final ProgramDetailsDto programDetailsDto = new ProgramDetailsDto();
				programDetailsDto.setProgramDbId(project.getUniqueID());
				programDetailsDto.setName(project.getProjectName());
				programDetailsDto.setLeadPerson(user.getName());
				programDetailsDto.setLeadPersonDbId(String.valueOf(project.getUserId()));
				programDetailsDto.setCropName(project.getCropType().getCropName());
				programDetailsDtoList.add(programDetailsDto);
			}
		} else {
			throw new ApiRuntimeException("Program not found.");
		}

		return programDetailsDtoList;
	}

	@Override
	public long countProgramsByFilter(final ProgramSearchRequest programSearchRequest) {
		return this.workbenchDataManager.countProjectsByFilter(programSearchRequest);
	}

	@Override
	public ProgramSummary getByUUIDAndCrop(final String crop, final String programUUID) {
		try {
			final Project workbenchProgram = this.workbenchDataManager.getProjectByUuidAndCrop(programUUID, crop);
			if (workbenchProgram != null) {
				final ProgramSummary programSummary = new ProgramSummary();
				programSummary.setId(workbenchProgram.getProjectId().toString());
				programSummary.setName(workbenchProgram.getProjectName());
				if (workbenchProgram.getCropType() != null) {
					programSummary.setCrop(workbenchProgram.getCropType().getCropName());
				}
				final WorkbenchUser programUser = this.userService.getUserById(workbenchProgram.getUserId());
				programSummary.setCreatedBy(programUser.getName());

				final List<WorkbenchUser> allProgramMembers =
					this.userService.getUsersByProjectId(workbenchProgram.getProjectId());
				final Set<String> members = new HashSet<>();
				for (final WorkbenchUser member : allProgramMembers) {
					members.add(member.getName());
				}
				programSummary.setMembers(members);
				programSummary.setUniqueID(workbenchProgram.getUniqueID());
				if (workbenchProgram.getStartDate() != null) {
					programSummary.setStartDate(ProgramServiceImpl.DATE_FORMAT.format(workbenchProgram.getStartDate()));
				}
				return programSummary;
			}
			return null;
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}

	}

	List<ProgramSummary> convertToProgramSummaries(final List<Project> workbenchProgramList) {
		final List<ProgramSummary> programSummaries = new ArrayList<>();
		for (final Project workbenchProgram : workbenchProgramList) {
			final ProgramSummary programSummary =
				new ProgramSummary(workbenchProgram.getProjectId().toString(), workbenchProgram.getUniqueID(),
					workbenchProgram.getProjectName(), workbenchProgram.getCropType().getCropName());

			final WorkbenchUser workbenchUser = this.userService.getUserById(workbenchProgram.getUserId());
			programSummary.setCreatedBy(workbenchUser.getName());

			final List<WorkbenchUser> workbenchUsers = this.userService
				.getUsersByProjectId(workbenchProgram.getProjectId());
			final Set<String> members = new HashSet<>();
			for (final WorkbenchUser member : workbenchUsers) {
				members.add(member.getName());
			}
			programSummary.setMembers(members);
			if (workbenchProgram.getStartDate() != null) {
				programSummary.setStartDate(ProgramServiceImpl.DATE_FORMAT.format(workbenchProgram.getStartDate()));
			}
			programSummaries.add(programSummary);
		}
		return programSummaries;
	}

	private void validateProgramSearchRequest(final ProgramSearchRequest programSearchRequest) {
		// Currently doesn't support Abbreviation
		if (!StringUtils.isBlank(programSearchRequest.getAbbreviation())) {
			throw new ApiRuntimeException("Program not found.");
		}

		if (!StringUtils.isEmpty(programSearchRequest.getCommonCropName())) {
			final List<CropType> cropTypeList = this.workbenchDataManager.getInstalledCropDatabses().stream().filter(cropType ->
				programSearchRequest.getCommonCropName().equalsIgnoreCase(cropType.getCropName())
			).collect(Collectors.toList());

			if (CollectionUtils.isEmpty(cropTypeList)) {
				throw new ApiRuntimeException("Crop " + programSearchRequest.getCommonCropName() + " doesn't exist.");
			}

			if (!Util.isNullOrEmpty(programSearchRequest.getLoggedInUserId())) {
				final List<CropType> authorizedCrop = this.workbenchDataManager.getAvailableCropsForUser(programSearchRequest
					.getLoggedInUserId()).stream().filter(cropType -> programSearchRequest.getCommonCropName()
					.equalsIgnoreCase(cropType.getCropName())
				).collect(Collectors.toList());

				if (CollectionUtils.isEmpty(authorizedCrop)) {
					throw new AccessDeniedException("Access Denied: User is not authorized for crop.");
				}
			}
		}

	}

}
