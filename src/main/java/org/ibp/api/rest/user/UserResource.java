package org.ibp.api.rest.user;

import io.swagger.annotations.ApiOperation;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.java.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Controller
public class UserResource {

	@Autowired
	private UserService userService;

	@ApiOperation(value = "List all users", notes = "List all users in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN')")
	public ResponseEntity<List<UserDetailDto>> listUsers() {
		return new ResponseEntity<>(this.userService.getAllUsersSortedByLastName(), HttpStatus.OK);
	}

	@ApiOperation(value = "Filter users", notes = "List all users in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/users/filter", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	public ResponseEntity<List<UserDetailDto>> filterUsers(@RequestParam final String cropName, @RequestParam final String programUUID) {
		return new ResponseEntity<>(this.userService.getUsersByProjectUUID(programUUID), HttpStatus.OK);
	}

	@ApiOperation(value = "Create user", notes = "Create user in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/users", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN')")
	public ResponseEntity<Map<String, Object>> createUser(@RequestBody final
	UserDetailDto user) {
		final Map<String, Object> map = this.userService.createUser(user);

		if (map.get("ERROR") != null) {
			return new ResponseEntity<>(map, HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(map, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update user", notes = "Update user in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/users/{id}", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN')")
	public ResponseEntity<Map<String, Object>> updateUser(final @PathVariable
	String id, @RequestBody final UserDetailDto user) {
		final Map<String, Object> map = this.userService.updateUser(user);
		if (map.get("ERROR") != null) {
			return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
}
