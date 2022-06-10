
package org.ibp.api.java.impl.middleware.program;

import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.program.ProgramBasicDetailsDto;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.api.program.ProgramFavoriteService;
import org.generationcp.middleware.dao.workbench.ProgramMembersSearchRequest;
import org.generationcp.middleware.domain.workbench.AddProgramMemberRequestDto;
import org.generationcp.middleware.domain.workbench.ProgramMemberDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ProgramLocationDefault;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.program.validator.AddProgramMemberRequestDtoValidator;
import org.ibp.api.java.impl.middleware.program.validator.ProgramBasicDetailsDtoValidator;
import org.ibp.api.java.impl.middleware.program.validator.RemoveProgramMembersValidator;
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
import java.util.Set;

@Service
@Transactional
public class ProgramServiceImpl implements ProgramService {

	@Autowired
	@Lazy
	private org.generationcp.middleware.api.program.ProgramService programServiceMw;

	@Autowired
	private UserService userService;

	@Autowired
	private AddProgramMemberRequestDtoValidator addProgramMemberRequestDtoValidator;

	@Autowired
	private RemoveProgramMembersValidator removeProgramMembersValidator;

	@Autowired
	private LocationService locationService;

	@Autowired
	private ProgramFavoriteService programFavoriteService;

	@Autowired
	private ProgramBasicDetailsDtoValidator programBasicDetailsDtoValidator;

	@Autowired
	private StudyService studyService;

	@Autowired
	private GermplasmListService germplasmListService;

	private final InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public List<ProgramDTO> listProgramsByCropName(final String cropName) {
		try {
			return this.convertToProgramSummaries(this.programServiceMw.getProjectsByCropName(cropName));
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public List<ProgramDTO> listProgramsByCropNameAndUser(final ProgramSearchRequest programSearchRequest) {
		try {
			return this.convertToProgramSummaries(this.programServiceMw.getProjects(null, programSearchRequest));
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	List<ProgramDTO> convertToProgramSummaries(final List<Project> workbenchProgramList) {
		final List<ProgramDTO> programSummaries = new ArrayList<>();
		for (final Project workbenchProgram : workbenchProgramList) {
			final ProgramDTO programSummary =
				new ProgramDTO(workbenchProgram.getProjectId().toString(), workbenchProgram.getUniqueID(),
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

	@Override
	public List<ProgramDetailsDto> getProgramDetailsByFilter(final Pageable pageable, final ProgramSearchRequest programSearchRequest) {
		final List<ProgramDetailsDto> programDetailsDtoList = new ArrayList<>();
		final List<Project> projectList = this.programServiceMw.getProjects(pageable, programSearchRequest);
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
		}
		return programDetailsDtoList;
	}

	@Override
	public long countProgramsByFilter(final ProgramSearchRequest programSearchRequest) {
		return this.programServiceMw.countFilteredPrograms(programSearchRequest);
	}

	@Override
	public List<ProgramDTO> getFilteredPrograms(final Pageable pageable, final ProgramSearchRequest programSearchRequest) {
		return this.programServiceMw.filterPrograms(programSearchRequest, pageable);
	}

	@Override
	public ProgramDTO getLastOpenedProject(final Integer userId) {
		return this.programServiceMw.getLastOpenedProject(userId);
	}

	@Override
	public void saveOrUpdateProjectUserInfo(final Integer userId, final String programUUID) {
		this.programServiceMw.saveOrUpdateProjectUserInfo(userId, programUUID);
	}

	@Override
	public ProgramDTO getByUUIDAndCrop(final String crop, final String programUUID) {
		try {
			final Project workbenchProgram = this.programServiceMw.getProjectByUuidAndCrop(programUUID, crop);
			if (workbenchProgram != null) {
				final ProgramDTO programSummary = new ProgramDTO();
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
				programSummary.setLastOpenDate(Util.formatDateAsStringValue(
					workbenchProgram.getLastOpenDate(),
					Util.FRONTEND_TIMESTAMP_FORMAT));
				final ProgramLocationDefault programLocationDefault = this.locationService.getProgramLocationDefault(programUUID);
				programSummary.setBreedingLocationDefaultId(programLocationDefault.getBreedingLocationId());
				programSummary.setStorageLocationDefaultId(programLocationDefault.getStorageLocationId());
				return programSummary;
			}
			return null;
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public List<ProgramMemberDto> getProgramMembers(
		final String programUUID, final ProgramMembersSearchRequest searchRequest,
		final Pageable pageable) {

		return this.userService.getProgramMembers(programUUID, searchRequest, pageable);
	}

	@Override
	public long countAllProgramMembers(final String programUUID, final ProgramMembersSearchRequest searchRequest) {
		return this.userService.countAllProgramMembers(programUUID, searchRequest);
	}

	@Override
	public void addNewProgramMembers(final String programUUID, final AddProgramMemberRequestDto requestDto) {
		this.addProgramMemberRequestDtoValidator.validate(programUUID, requestDto);
		this.programServiceMw.addProgramMembers(programUUID, requestDto);
	}

	@Override
	public void removeProgramMembers(final String programUUID, final Set<Integer> userIds) {
		this.removeProgramMembersValidator.validate(programUUID, userIds);
		this.programServiceMw.removeProgramMembers(programUUID, new ArrayList<>(userIds));
	}

	@Override
	public ProgramDTO createProgram(final String crop, final ProgramBasicDetailsDto programBasicDetailsDto) {
		this.programBasicDetailsDtoValidator.validateCreation(crop, programBasicDetailsDto);

		final ProgramDTO programDTO = this.programServiceMw.addProgram(crop, programBasicDetailsDto);

		this.locationService.saveProgramLocationDefault(programDTO.getUniqueID(), programBasicDetailsDto.getBreedingLocationDefaultId(),
			programBasicDetailsDto.getStorageLocationDefaultId());
		programDTO.setBreedingLocationDefaultId(programBasicDetailsDto.getBreedingLocationDefaultId());
		programDTO.setStorageLocationDefaultId(programBasicDetailsDto.getStorageLocationDefaultId());
		this.installationDirectoryUtil.createWorkspaceDirectoriesForProject(crop, programBasicDetailsDto.getName());

		return programDTO;
	}

	@Override
	public void deleteProgram(final String programUUID) {
		this.studyService.deleteProgramStudies(programUUID);
		this.programFavoriteService.deleteAllProgramFavorites(programUUID);
		this.germplasmListService.deleteProgramGermplasmLists(programUUID);
		this.programServiceMw.deleteProgramAndDependencies(programUUID);
	}

	@Override
	public boolean editProgram(final String cropName, final String programUUID, final ProgramBasicDetailsDto programBasicDetailsDto) {
		final String oldProjectName = this.programServiceMw.getProgramByUUID(programUUID).get().getName();
		this.programBasicDetailsDtoValidator.validateEdition(cropName, programUUID, programBasicDetailsDto);
		if (programBasicDetailsDto.allAttributesNull()) {
			return false;
		}
		this.programServiceMw.editProgram(programUUID, programBasicDetailsDto);
		this.installationDirectoryUtil.renameOldWorkspaceDirectory(oldProjectName, cropName, programBasicDetailsDto.getName());
		if (programBasicDetailsDto.getBreedingLocationDefaultId() != null || programBasicDetailsDto.getStorageLocationDefaultId() != null) {
			this.locationService.updateProgramLocationDefault(programUUID, programBasicDetailsDto);
		}
		return true;
	}
}
