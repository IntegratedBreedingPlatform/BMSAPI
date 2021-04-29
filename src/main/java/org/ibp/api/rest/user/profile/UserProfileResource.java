package org.ibp.api.rest.user.profile;

import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.domain.user.UserProfileDto;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

	@ApiOperation(value = "Update user profile", notes = "Update First name, Laste name and Email from user profile in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/users/profile", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Integer> updateProfile(
		@RequestBody final UserProfileDto userProfileDto) {

		try {
			final UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(userProfileDto.getUserName(), userProfileDto.getPassword());
			final Authentication authentication = this.authenticationManager.authenticate(credentials);
		} catch (final AuthenticationException e) {
			throw new ApiRuntimeException("The password is wrong, please check");
		}
		final WorkbenchUser workbenchUser = this.userServiceMiddleware.getUserByUsername(userProfileDto.getUserName());
		return new ResponseEntity<>(this.userService.updateUserProfile(userProfileDto, workbenchUser), HttpStatus.OK);
	}
}
