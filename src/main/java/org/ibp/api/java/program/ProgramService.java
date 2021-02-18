
package org.ibp.api.java.program;

import java.util.List;

import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.ibp.api.domain.program.ProgramSummary;

public interface ProgramService {

	List<ProgramSummary> listProgramsByCropName(final String cropName);

	List<ProgramSummary> listProgramsByCropNameAndUser(WorkbenchUser user, String cropName);

	List<ProgramDetailsDto> getProgramsByFilter(final int pageNumber, final int pageSize, final ProgramSearchRequest programSearchRequest);

	long countProgramsByFilter(final ProgramSearchRequest programSearchRequest);

	ProgramSummary getByUUIDAndCrop(String crop, String programUUID);
}
