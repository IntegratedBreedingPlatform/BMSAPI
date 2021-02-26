
package org.ibp.api.java.program;

import java.util.List;

import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.ibp.api.domain.program.ProgramSummary;
import org.springframework.data.domain.Pageable;

public interface ProgramService {

	List<ProgramSummary> listProgramsByCropName(final String cropName);

	List<ProgramSummary> listProgramsByCropNameAndUser(ProgramSearchRequest programSearchRequest);

	List<ProgramDetailsDto> getProgramsByFilter(final Pageable pageable, final ProgramSearchRequest programSearchRequest);

	List<ProgramDTO> listPrograms(Pageable pageable);

	List<ProgramDTO> listProgramsByUser(Pageable pageable, WorkbenchUser user);

	long countPrograms();

	long countProgramsByUser(WorkbenchUser currentlyLoggedInUser);

	long countProgramsByFilter(final ProgramSearchRequest programSearchRequest);

	ProgramSummary getByUUIDAndCrop(String crop, String programUUID);
}
