package org.generationcp.bms.program;

import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/program")
public class ProgramResource {
	
	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public List<Project> listPrograms() throws MiddlewareQueryException {
		return this.workbenchDataManager.getProjects();
	}
}
