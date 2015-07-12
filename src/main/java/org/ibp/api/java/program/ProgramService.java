
package org.ibp.api.java.program;

import java.util.List;

import org.ibp.api.domain.program.ProgramSummary;

public interface ProgramService {

	List<ProgramSummary> listAllPrograms();
}
