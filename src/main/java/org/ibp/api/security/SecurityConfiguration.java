package org.ibp.api.security;

import org.generationcp.middleware.pojos.workbench.Role;
import org.ibp.api.security.xauth.TokenProvider;
import org.ibp.api.security.xauth.XAuthTokenConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// **Important note for developers** : This class is central to the authentication framework of BMSAPI. Please do not alter it without a
// good understanding of Spring Security in general and BMS X-Auth-Token based authentication workflow in particular, otherwise there will
// be MAJOR breakages in the functioning of BMS components. Consult your friendly senior developer first if you are unsure.
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private Http401UnauthorizedEntryPoint authenticationEntryPoint;

	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private TokenProvider tokenProvider;

	/**
	 * Workbench uses BCrypt password encryption.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Autowired
	public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(this.userDetailsService).passwordEncoder(this.passwordEncoder());
	}

	@Override
	public void configure(final WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/static/**").antMatchers("/v2/api-docs/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.exceptionHandling()
		.authenticationEntryPoint(this.authenticationEntryPoint)
		.and()
		.csrf().disable()
		.headers()
		.frameOptions().disable()
		.and()
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
		.authorizeRequests()
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.antMatchers("/",
					"/v2/api-docs/**",
					"/authenticate",
					"/token",
					"/brapi/**/token",
					"/breeding_view/**",
					"/**/brapi/authorize",
					"/**/.well-known/openid-configuration"
				).permitAll()
			.anyRequest()
			.authenticated()
		.and()
		.apply(this.securityConfigurerAdapter())
		.and()
		.servletApi().rolePrefix("");
	}

	private XAuthTokenConfigurer securityConfigurerAdapter() {
		return new XAuthTokenConfigurer(this.userDetailsService, this.tokenProvider);
	}
}
