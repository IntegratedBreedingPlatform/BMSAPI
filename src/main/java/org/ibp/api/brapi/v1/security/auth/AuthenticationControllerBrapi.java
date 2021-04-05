
package org.ibp.api.brapi.v1.security.auth;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.security.xauth.Token;
import org.ibp.api.security.xauth.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a> Authentication services.
 *
 */
@Api(value = "BrAPI Authentication Service")
@Controller
public class AuthenticationControllerBrapi {

	@Autowired
	private TokenProvider tokenProvider;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService userDetailsService;

	@ApiOperation(value = "Get token")
	@RequestMapping(value = {"/brapi/v1/token", "/brapi/v2/token", "/token"}, method = RequestMethod.POST)
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

	@RequestMapping(value = "/brapi/authorize", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity authorize(@RequestParam(value = "display_name") final String display_name,@RequestParam(value = "return_url") final String return_url)
		throws UnsupportedEncodingException {
		final URI loginUrl = URI.create(
			"/ibpworkbench/controller/auth/login?display_name=" + URLEncoder.encode(display_name,"UTF-8") + "&return_url=" + return_url);
		return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(loginUrl).build();
	}

	/**
	 * XXX workaround for systems that expect only one brapi base url (e.g. Field book)
	 */
	@ApiOperation(value = "Same as /authorize. crop is ignored")
	@RequestMapping(value = "/{crop}/brapi/authorize", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity authorize2(
		@PathVariable final String crop,
		@RequestParam(value = "display_name") final String display_name,
		@RequestParam(value = "return_url") final String return_url)
		throws UnsupportedEncodingException {
		final URI loginUrl = URI.create(
			"/ibpworkbench/controller/auth/login?display_name=" + URLEncoder.encode(display_name,"UTF-8") + "&return_url=" + return_url);
		return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(loginUrl).build();
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
