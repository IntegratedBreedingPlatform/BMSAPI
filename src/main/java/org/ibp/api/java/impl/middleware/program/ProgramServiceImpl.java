
package org.ibp.api.java.impl.middleware.program;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.program.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProgramServiceImpl implements ProgramService {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private SecurityService securityService;

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public List<ProgramSummary> listAllPrograms() {
		List<Project> workbenchProgramList;
		List<ProgramSummary> programSummaries = new ArrayList<>();
		try {
			User loggedInUser = this.securityService.getCurrentlyLoggedInUser();
			workbenchProgramList = this.workbenchDataManager.getProjectsByUser(loggedInUser);
			if (!workbenchProgramList.isEmpty()) {
				for (Project workbenchProgram : workbenchProgramList) {
					ProgramSummary programSummary = new ProgramSummary();
					programSummary.setId(workbenchProgram.getProjectId().toString());
					programSummary.setName(workbenchProgram.getProjectName());
					if(workbenchProgram.getCropType() != null ) {
						programSummary.setCrop(workbenchProgram.getCropType().getCropName());
					}
					User programUser = this.workbenchDataManager.getUserById(workbenchProgram.getUserId());
					programSummary.setCreatedBy(programUser.getName());

					List<User> allProgramMembers = this.workbenchDataManager.getUsersByProjectId(workbenchProgram.getProjectId());
					Set<String> members = new HashSet<>();
					for (User member : allProgramMembers) {
						members.add(member.getName());
					}
					programSummary.setMembers(members);
					programSummary.setUniqueID(workbenchProgram.getUniqueID());
					if (workbenchProgram.getStartDate() != null) {
						programSummary.setStartDate(ProgramServiceImpl.DATE_FORMAT.format(workbenchProgram.getStartDate()));
					}
					programSummaries.add(programSummary);
				}
			}
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
		return programSummaries;
	}

}
