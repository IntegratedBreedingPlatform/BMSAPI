package org.ibp.api.rest.user;

import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.java.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Controller
public class UserResource {

	@Autowired
	private UserService userService;

	@ApiOperation(value = "List all users of one projectUUID", notes = "List all users")
	@RequestMapping(value = "/user/list", method = RequestMethod.GET)
	public ResponseEntity<List<UserDetailDto>> list(@RequestParam(value = "projectUUID") final String projectUUID) {
		return new ResponseEntity<>(this.userService.getUsersByProjectUUID(projectUUID), HttpStatus.OK);
	}
}
