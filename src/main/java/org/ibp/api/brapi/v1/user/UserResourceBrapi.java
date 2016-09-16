
package org.ibp.api.brapi.v1.user;

import java.util.List;

import org.ibp.api.domain.common.GenericResponse;
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
		return new ResponseEntity<List<UserDetailDto>>(this.userService.getAllUserDtosSorted(), HttpStatus.OK);
	}

	@ApiOperation(value = "Create user", notes = "Create user in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/brapi/v1/users", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> createUser(@RequestBody UserDetailsDto user) {
		GenericResponse response = this.userService.createUser(user);
		if ("0".equals(response.getId())) {
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		}
		return new ResponseEntity<GenericResponse>(response, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update user", notes = "Update user in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/brapi/v1/users/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<GenericResponse> updateUserfinal(final @PathVariable String id, @RequestBody UserDetailsDto user) {
		GenericResponse response = this.userService.updateUser(user);
		if (!id.equals(response.getId())) {
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
	}
}
