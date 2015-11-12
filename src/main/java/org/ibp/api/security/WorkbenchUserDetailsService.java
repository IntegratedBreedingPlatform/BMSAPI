
package org.ibp.api.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class WorkbenchUserDetailsService implements UserDetailsService {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	public WorkbenchUserDetailsService() {

	}

	public WorkbenchUserDetailsService(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			// username must be converted from html-encode to utf-8 string to support chinese/utf-8 languages
			username = StringEscapeUtils.unescapeHtml(username);

			List<User> matchingUsers = this.workbenchDataManager.getUserByName(username, 0, 1, Operation.EQUAL);
			if (matchingUsers != null && !matchingUsers.isEmpty()) {
				User workbenchUser = matchingUsers.get(0);
				// FIXME Populate flags for accountNonExpired, credentialsNonExpired, accountNonLocked properly, all true for now.
				return new org.springframework.security.core.userdetails.User(workbenchUser.getName(), workbenchUser.getPassword(),
						this.getRolesAsAuthorities(workbenchUser));
			}
			throw new UsernameNotFoundException("Invalid username/password.");
		} catch (MiddlewareQueryException e) {
			throw new AuthenticationServiceException("Data access error while authenticaing user against Workbench.", e);
		}
	}

	private Collection<? extends GrantedAuthority> getRolesAsAuthorities(User workbenchUser) {
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		if (workbenchUser != null) {
			List<UserRole> userRoles = workbenchUser.getRoles();
			if (userRoles != null && !userRoles.isEmpty()) {
				for (UserRole role : userRoles) {
					authorities.add(new SimpleGrantedAuthority(role.getRole()));
				}
			}
		}
		return authorities;
	}
}
