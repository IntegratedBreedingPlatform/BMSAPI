package org.ibp.api.java.tool;

import org.generationcp.middleware.domain.workbench.ToolDTO;

import java.util.List;

public interface ToolService {

	List<ToolDTO> getTools(final String cropName, final Integer programId);

}
