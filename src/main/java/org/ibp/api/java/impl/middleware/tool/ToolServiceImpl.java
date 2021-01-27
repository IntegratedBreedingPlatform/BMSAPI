package org.ibp.api.java.impl.middleware.tool;

import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.domain.workbench.ToolDTO;
import org.generationcp.middleware.domain.workbench.ToolLinkDTO;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.WorkbenchSidebarCategory;
import org.generationcp.middleware.pojos.workbench.WorkbenchSidebarCategoryLink;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.workbench.WorkbenchService;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.tool.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

	@Override
	public List<ToolDTO> getTools(final String cropName, final Integer programId) {

		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();

		final List<PermissionDto> permissions =
			this.permissionService.getPermissionLinks(loggedInUser.getUserid(), cropName, programId);

		// get all categories first
		final Map<WorkbenchSidebarCategory, List<WorkbenchSidebarCategoryLink>> categoryMap = new LinkedHashMap<>();
		final Set<WorkbenchSidebarCategoryLink> links = new HashSet<>();

		for (final PermissionDto permission : permissions) {
			final WorkbenchSidebarCategoryLink link =
				this.workbenchService.getWorkbenchSidebarLinksByCategoryId(permission.getWorkbenchCategoryLinkId());
			if (link != null && !links.contains(link)) {
				if (categoryMap.get(link.getWorkbenchSidebarCategory()) == null) {
					categoryMap.put(link.getWorkbenchSidebarCategory(), new ArrayList<>());
				}
				if (link.getTool() == null) {
					link.setTool(new Tool(link.getSidebarLinkName(), link.getSidebarLinkTitle(), ""));
				}
				categoryMap.get(link.getWorkbenchSidebarCategory()).add(link);
				links.add(link);
			}
		}

		//Convert HashMap to TreeMap.It will be sorted in natural order.
		final Map<WorkbenchSidebarCategory, List<WorkbenchSidebarCategoryLink>> treeMap = new TreeMap<>( categoryMap );

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

}
