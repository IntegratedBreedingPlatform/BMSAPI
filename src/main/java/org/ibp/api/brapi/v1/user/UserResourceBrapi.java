
package org.ibp.api.brapi.v1.user;

import java.util.List;
import java.util.Map;

import org.ibp.api.java.impl.middleware.user.UserDetailDto;
import org.ibp.api.java.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "BrAPI User Services")
@Controller
public class UserResourceBrapi {

	@Autowired
	private UserService userService;

	@ApiOperation(value = "List all users", notes = "List all users in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/brapi/v1/users", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<UserDetailDto>> listUsers() {
		return new ResponseEntity<>(this.userService.getAllUsersSortedByLastName(), HttpStatus.OK);
	}

	@ApiOperation(value = "Create user", notes = "Create user in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/brapi/v1/users", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserDetailDto user) {
		final Map<String, Object> map = this.userService.createUser(user);

		if (map.get("ERROR") != null) {
			return new ResponseEntity<>(map, HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(map, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update user", notes = "Update user in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/brapi/v1/users/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> updateUser(final @PathVariable String id, @RequestBody UserDetailDto user) {
		Map<String, Object> map = this.userService.updateUser(user);
		if (map.get("ERROR") != null) {
			return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
}
