package org.ibp.api.java.impl.middleware.tool;

import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.api.role.RoleService;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.domain.workbench.ToolDTO;
import org.generationcp.middleware.domain.workbench.ToolLinkDTO;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.pojos.workbench.WorkbenchSidebarCategory;
import org.generationcp.middleware.pojos.workbench.WorkbenchSidebarCategoryLink;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.workbench.WorkbenchService;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.tool.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class ToolServiceImpl implements ToolService {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private WorkbenchService workbenchService;

	@Autowired
	public ProgramValidator programValidator;

	@Autowired
	public RoleService roleService;

	@Autowired
	public ProgramService programService;

	private BindingResult errors;

	@Override
	public List<ToolDTO> getTools(final String cropName, final String programUUID) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());

		this.validateProgram(cropName, programUUID);

		final Project project = this.programService.getProjectByUuid(programUUID);
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		final List<PermissionDto> permissions =
			this.permissionService.getPermissionLinks(loggedInUser.getUserid(), cropName, project.getProjectId().intValue());

		// get all categories first
		final Map<WorkbenchSidebarCategory, List<WorkbenchSidebarCategoryLink>> categoryMap = new LinkedHashMap<>();
		final Set<WorkbenchSidebarCategoryLink> links = new HashSet<>();

		for (final PermissionDto permission : permissions) {
			final WorkbenchSidebarCategoryLink link =
				this.workbenchService.getWorkbenchSidebarLinksByCategoryId(permission.getWorkbenchCategoryLinkId());

			if (link != null && !links.contains(link)) {
				if (link.getTool() == null) {
					link.setTool(new Tool(link.getSidebarLinkName(), link.getSidebarLinkTitle(), ""));
				}
				if (!ToolName.GRAPHICAL_QUERIES.getName().equals(link.getTool().getToolName())
					|| this.showGraphicalQuery(loggedInUser, cropName)) {
					if (categoryMap.get(link.getWorkbenchSidebarCategory()) == null) {
						categoryMap.put(link.getWorkbenchSidebarCategory(), new ArrayList<>());
					}

					categoryMap.get(link.getWorkbenchSidebarCategory()).add(link);
					links.add(link);
				}
			}
		}

		//Convert HashMap to TreeMap.It will be sorted in natural order.
		final Map<WorkbenchSidebarCategory, List<WorkbenchSidebarCategoryLink>> treeMap = new TreeMap<>(categoryMap);

		//sorting the list with a comparator
		for (final WorkbenchSidebarCategory category : treeMap.keySet()) {
			Collections.sort(categoryMap.get(category));
		}

		return treeMap.entrySet().stream()
			.map(e -> {
				final List<ToolLinkDTO> toolLinkDTOS = e.getValue().stream()
					.map(workbenchSidebarCategoryLink -> new ToolLinkDTO(workbenchSidebarCategoryLink.getSidebarLinkTitle(),
						workbenchSidebarCategoryLink.getTool().getPath()))
					.collect(Collectors.toList());

				return new ToolDTO(e.getKey().getSidebarCategorylabel(), toolLinkDTOS);
			}).collect(Collectors.toList());
	}

	private void validateProgram(final String cropName, final String programUUID) {
		if (Objects.isNull(programUUID)) {
			this.errors.reject("program.does.not.exist", "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		this.programValidator.validate(new ProgramDTO(cropName, programUUID), this.errors);
		if (this.errors.hasErrors()) {
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
	}

	private boolean showGraphicalQuery(final WorkbenchUser loggedInUser, final String cropName) {
		return !loggedInUser.hasOnlyProgramRoles(cropName);
	}
}
