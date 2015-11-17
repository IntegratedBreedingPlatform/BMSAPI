
package org.ibp.api.security.xauth;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class XAuthTokenConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	private final TokenProvider tokenProvider;

	private final UserDetailsService detailsService;

	public XAuthTokenConfigurer(final UserDetailsService detailsService, final TokenProvider tokenProvider) {
		this.detailsService = detailsService;
		this.tokenProvider = tokenProvider;
	}

	@Override
	public void configure(final HttpSecurity http) throws Exception {
		final XAuthTokenFilter customFilter = new XAuthTokenFilter(this.detailsService, this.tokenProvider);
		http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
