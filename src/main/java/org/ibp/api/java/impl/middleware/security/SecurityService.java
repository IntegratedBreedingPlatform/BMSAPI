
package org.ibp.api.java.impl.middleware.security;

import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudySummary;

public interface SecurityService {

	/**
	 * Determines whether the given study is accessible to currently logged in user. Users can only access studies that belong to programs
	 * that they have created or are member of.
	 */
	boolean isAccessible(StudySummary study, String cropname);

	/**
	 * Determines whether the given germplasm list is accessible to currently logged in user. Germplasm list is accessible if the logged in
	 * user is owner of the list, or the list belongs to a program that the logged in user is a member of. Lists with no program unique id
	 * reference are accessible to all as these are usually loaded from historic data.
	 */
	boolean isAccessible(GermplasmList germplasmList, String cropname);

	/**
	 * Retrieves the workbench user details based on details of the logged in user in Spring security context.
	 */
	WorkbenchUser getCurrentlyLoggedInUser();

}
