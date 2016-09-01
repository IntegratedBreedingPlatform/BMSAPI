
package org.ibp.api.java.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/users")
public class UsersApi {

	@Autowired
	private UserService userService;

	@ApiOperation(value = "List all users", notes = "List all users in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/listUsers", method = RequestMethod.GET)
	public ResponseEntity<List<UserData>> listUsers() {
		return new ResponseEntity<List<UserData>>(this.userService.getAllUserDtosSorted(), HttpStatus.OK);
	}

}
