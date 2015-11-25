
package org.ibp.api.java.impl.middleware.security;

import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.service.api.study.StudySummary;

public interface SecurityService {

	/**
	 * Determines whether the given study is accessible to currently logged in user. Users can only access studies that belong to programs
	 * that they have created or are member of.
	 */
	boolean isAccessible(StudySummary study);

	/**
	 * Retrieves the workbench user details based on details of the logged in user in Spring security context.
	 */
	User getCurrentlyLoggedInUser();

}
