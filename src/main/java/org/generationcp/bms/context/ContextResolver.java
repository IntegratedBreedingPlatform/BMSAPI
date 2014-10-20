package org.generationcp.bms.context;

import org.generationcp.middleware.pojos.workbench.Project;

public interface ContextResolver {
	
	Project resolveProgram() throws ContextResolutionException;
}
