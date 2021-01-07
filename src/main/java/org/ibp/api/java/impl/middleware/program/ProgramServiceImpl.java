
package org.ibp.api.java.impl.middleware.program;

import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.generationcp.middleware.service.api.program.ProgramFilters;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.program.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			return convertToProgramSummaries(this.workbenchDataManager.getProjectsByCropName(cropName));
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public List<ProgramSummary> listProgramsByCropNameAndUser(final WorkbenchUser user, final String cropName) {
		try {
			return convertToProgramSummaries(this.workbenchDataManager.getProjectsByUser(user, cropName));
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public List<ProgramDTO> listPrograms(final Pageable pageable) {
		return this.programService.listPrograms(pageable);
	}

	@Override
	public List<ProgramDTO> listProgramsByUser(final Pageable pageable, final WorkbenchUser user) {
		// TODO
		return null;
	}

	@Override
	public long countPrograms() {
		return this.programService.countPrograms();
	}

	@Override
	public long countProgramsByUser(final WorkbenchUser currentlyLoggedInUser) {
		// TODO
		return 0;
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

	public List<ProgramDetailsDto> getProgramsByFilter(final int pageNumber, final int pageSize,
		final Map<ProgramFilters, Object> filters) {
		final List<ProgramDetailsDto> programDetailsDtoList = new ArrayList<>();
		final List<Project> projectList = this.workbenchDataManager.getProjects(pageNumber, pageSize, filters);

		if (!projectList.isEmpty()) {
			for (final Project project : projectList) {
				final ProgramDetailsDto programDetailsDto = new ProgramDetailsDto();
				programDetailsDto.setProgramDbId(project.getUniqueID());
				programDetailsDto.setName(project.getProjectName());
				programDetailsDtoList.add(programDetailsDto);
			}
		}

		return programDetailsDtoList;
	}

	public long countProgramsByFilter(final Map<ProgramFilters, Object> filter) {
		return this.workbenchDataManager.countProjectsByFilter(filter);
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
}
