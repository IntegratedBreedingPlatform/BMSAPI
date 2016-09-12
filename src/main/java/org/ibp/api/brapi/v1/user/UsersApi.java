
package org.ibp.api.brapi.v1.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "BrAPI User Services")
@Controller
public class UsersApi {

	@Autowired
	private UserService userService;

	@ApiOperation(value = "List all users", notes = "List all users in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/brapi/v1/users", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<UserData>> listUsers() {
		return new ResponseEntity<List<UserData>>(this.userService.getAllUserDtosSorted(), HttpStatus.OK);
	}

}
