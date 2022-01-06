
package org.ibp.api.java.program;

import org.generationcp.middleware.api.program.ProgramBasicDetailsDto;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.workbench.AddProgramMemberRequestDto;
import org.generationcp.middleware.domain.workbench.ProgramMemberDto;
import org.generationcp.middleware.domain.workbench.UserSearchRequest;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface ProgramService {

	List<ProgramDTO> listProgramsByCropName(String cropName);

	List<ProgramDTO> listProgramsByCropNameAndUser(ProgramSearchRequest programSearchRequest);

	long countProgramsByFilter(ProgramSearchRequest programSearchRequest);

	ProgramDTO getByUUIDAndCrop(String crop, String programUUID);

	List<ProgramDetailsDto> getProgramDetailsByFilter(Pageable pageable, ProgramSearchRequest programSearchRequest);

	List<ProgramDTO> getFilteredPrograms(Pageable pageable, ProgramSearchRequest programSearchRequest);

	ProgramDTO getLastOpenedProject(Integer userId);

	void saveOrUpdateProjectUserInfo(Integer userId, String  programUUID);

	List<ProgramMemberDto> getProgramMembers(String programUUID, UserSearchRequest userSearchRequest, Pageable pageable);

	long countAllProgramMembers(String programUUID, UserSearchRequest userSearchRequest);

	void addNewProgramMembers(String programUUID, AddProgramMemberRequestDto requestDto);

	void removeProgramMembers(String programUUID, Set<Integer> userIds);

	ProgramDTO createProgram(String crop, ProgramBasicDetailsDto programBasicDetailsDto);

	void deleteProgram(String programUUID);

	boolean editProgram(String cropName, String programUUID, ProgramBasicDetailsDto programBasicDetailsDto);
}
