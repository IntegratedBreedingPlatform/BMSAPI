package org.ibp.api.rest.account;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.ContextHolder;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.java.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api("Account Services")
@RestController
public class AccountResource {

	@Autowired
	private UserService userService;

	/**
	 * @param cropName not mandatory given that instance roles are not per crop
	 * @param programUUID not mandatory given that instance and program roles are not per program
	 * @return the logged-in user account with the authorities for crop/program (if set)
	 */
	@RequestMapping(value = "/account", method = RequestMethod.GET)
	public UserDetailDto getAccount(
		@ApiParam("to populate the user permissions per crop") @RequestParam(required = false) final String cropName,
		@ApiParam("to populate the user permissions per program") @RequestParam(required = false) final String programUUID) {
		ContextHolder.setCurrentCrop(cropName);
		return this.userService.getUserWithAuthorities(cropName, programUUID);
	}
}
