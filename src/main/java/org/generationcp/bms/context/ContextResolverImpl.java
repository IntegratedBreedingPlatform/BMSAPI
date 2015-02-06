package org.generationcp.bms.context;

import org.generationcp.bms.Constants;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Component
public class ContextResolverImpl implements ContextResolver {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextResolverImpl.class);

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public Project resolveProgram() throws ContextResolutionException {

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		HttpSession session = request.getSession();
		Project selectedProgram = (Project) session.getAttribute(Constants.PARAM_SELECTED_PROGRAM);

		if (selectedProgram != null) {
			LOGGER.debug("Resolved program from session: " + selectedProgram);
			return selectedProgram;
		}

		try {
			Project lastOpenedProgram = this.workbenchDataManager.getLastOpenedProjectAnyUser();
			if (lastOpenedProgram != null) {
				LOGGER.debug("Resolved program from workbench access history: " + lastOpenedProgram);
				return lastOpenedProgram;
			}
		} catch (MiddlewareQueryException e) {
			throw new ContextResolutionException("Data access error while trying to resolve program from workbench access history.", e);
		}
		throw new ContextResolutionException("Unable to resolve program in context.");
	}
}
