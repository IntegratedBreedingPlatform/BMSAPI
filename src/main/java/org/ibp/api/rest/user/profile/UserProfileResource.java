package org.ibp.api.rest.user.profile;

import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.domain.user.UserProfileUpdateRequestDTO;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Controller
public class UserProfileResource {

	@Autowired
	private UserService userService;

	@Autowired
	private org.generationcp.middleware.service.api.user.UserService userServiceMiddleware;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private SecurityService securityService;

	@ApiOperation(value = "Update user profile", notes = "Update First name, Last name and Email from user profile in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "my-profile", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Void> updateProfile(
		@RequestBody final UserProfileUpdateRequestDTO userProfileUpdateRequestDTO) {

		try {
			final UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(this.securityService.getCurrentlyLoggedInUser().getName(), userProfileUpdateRequestDTO
				.getPassword());
			this.authenticationManager.authenticate(credentials);
		} catch (final AuthenticationException e) {
			throw new ApiRuntimeException("The password is wrong, please check");
		}
		final WorkbenchUser workbenchUser = this.securityService.getCurrentlyLoggedInUser();
		this.userService.updateUserProfile(userProfileUpdateRequestDTO, workbenchUser);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
