package org.ibp.api.rest.user;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.user.UserSearchRequest;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.user.UserService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@Controller
public class UserResource {

	@Autowired
	private UserService userService;

	@ApiOperation(value = "List all users", notes = "List all users in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN')")
	public ResponseEntity<List<UserDto>> listUsers() {
		return new ResponseEntity<>(this.userService.getAllUsersSortedByLastName(), HttpStatus.OK);
	}

	@ApiOperation(value = "Filter users", notes = "List all users in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/users/filter", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	public ResponseEntity<List<UserDto>> filterUsers(@RequestParam final String cropName, @RequestParam final String programUUID) {
		return new ResponseEntity<>(this.userService.getUsersByProjectUUID(programUUID), HttpStatus.OK);
	}

	@ApiOperation(value = "Create user", notes = "Create user in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/users", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN')")
	public ResponseEntity<Integer> createUser(@RequestBody final
	UserDto user) {
		return new ResponseEntity<>(this.userService.createUser(user), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update user", notes = "Update user in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/users/{id}", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN')")
	public ResponseEntity<Integer> updateUser(final @PathVariable
	String id, @RequestBody final UserDto user) {
		return new ResponseEntity<>(this.userService.updateUser(user), HttpStatus.OK);
	}

	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.")
	})
	@ApiOperation(value = "List users", notes = "Get a list of users filter by user Name, first name, last name, email, crop, roles and status")
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN')")
	@RequestMapping(value = "/users/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<UserDto>> searchUsers(@RequestBody final UserSearchRequest userSearchRequest,
		@ApiIgnore @PageableDefault(page = 0, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable){
		return new PaginatedSearch().getPagedResult(() -> this.userService.countSearchUsers(userSearchRequest),
			() -> this.userService.searchUsers(userSearchRequest, pageable), pageable);
	}
}
