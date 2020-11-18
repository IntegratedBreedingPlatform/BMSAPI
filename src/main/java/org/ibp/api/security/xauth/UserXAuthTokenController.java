
package org.ibp.api.security.xauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

// **Important note for developers** : This class is central to the authentication framework of BMSAPI. Please do not alter it without a
// good understanding of Spring Security in general and BMS X-Auth-Token based authentication workflow in particular, otherwise there will
// be MAJOR breakages in the functioning of BMS components. Consult your friendly senior developer first if you are unsure.
@RestController
public class UserXAuthTokenController {

	@Autowired
	private TokenProvider tokenProvider;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService userDetailsService;

	@Deprecated
	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	@ApiIgnore
	public Token authenticate(@RequestParam final String username, @RequestParam final String password) {
		final UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(username, password);
		final Authentication authentication = this.authenticationManager.authenticate(credentials);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		final UserDetails details = this.userDetailsService.loadUserByUsername(username);
		return this.tokenProvider.createToken(details);
	}

	// This is an empty web method created for the purpose of validating the authorization token.
	// Like in every web method, the system checks the validity of the token before this is executed.
	// If the token invalid, this will return a '401 Unauthorized' http response.
	@RequestMapping(value = "/validateToken", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@ApiIgnore
	public void validateToken() {
	}

	public void setTokenProvider(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

}
