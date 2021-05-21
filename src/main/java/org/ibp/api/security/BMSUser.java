package org.ibp.api.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class BMSUser extends User {

	private final Integer userId;

	public BMSUser(final Integer userId, final String username, final String password,
		final Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
		this.userId = userId;
	}

	public Integer getUserId() {
		return this.userId;
	}
}
