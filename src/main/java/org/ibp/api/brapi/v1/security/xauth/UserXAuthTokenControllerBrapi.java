
package org.ibp.api.brapi.v1.security.xauth;

import java.net.URL;

import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.security.xauth.Token;
import org.ibp.api.security.xauth.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a> Authentication services.
 *
 */
@Api(value = "BrAPI Authentication Service")
@Controller
public class UserXAuthTokenControllerBrapi {

	@Autowired
	private TokenProvider tokenProvider;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService userDetailsService;

	@ApiOperation(value = "Get X-Auth token")
	@RequestMapping(value = "/brapi/v1/token", method = RequestMethod.POST)
	@ResponseBody
	public TokenResponse authenticate(@RequestBody final TokenRequest tokenRequest) {
		final String username = tokenRequest.getUsername();
		final String password = tokenRequest.getPassword();

		final UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(username, password);
		final Authentication authentication = this.authenticationManager.authenticate(credentials);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		final UserDetails details = this.userDetailsService.loadUserByUsername(username);
		final Token token = this.tokenProvider.createToken(details);

		return new TokenResponse(new Metadata(null, null, new URL[] {}), username, token.getToken(), token.getExpires());
	}

	public void setTokenProvider(final TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public void setUserDetailsService(final UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}
}
