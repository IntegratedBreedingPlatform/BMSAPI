
package org.ibp.api.java.impl.middleware.program;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.java.program.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProgramServiceImpl implements ProgramService {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public List<ProgramSummary> listAllPrograms() {
		List<Project> workbenchProgramList;
		List<ProgramSummary> programSummaries = new ArrayList<>();
		try {
			workbenchProgramList = this.workbenchDataManager.getProjects();
			if (!workbenchProgramList.isEmpty()) {
				for (Project workbenchProgram : workbenchProgramList) {
					ProgramSummary programSummary = new ProgramSummary();
					programSummary.setId(workbenchProgram.getProjectId().toString());
					programSummary.setProjectName(workbenchProgram.getProjectName());
					programSummary.setCropType(workbenchProgram.getCropType().getCropName());
					programSummary.setUserId(String.valueOf(workbenchProgram.getUserId()));
					programSummary.setUniqueID(workbenchProgram.getUniqueID());
					if (workbenchProgram.getStartDate() != null) {
						programSummary.setStartDate(DATE_FORMAT.format(workbenchProgram.getStartDate()));
					}
					programSummaries.add(programSummary);
				}
			}
		} catch (MiddlewareQueryException e) {
			throw new RuntimeException("Error! Caused by: " + e.getMessage(), e);
		}
		return programSummaries;
	}

}
