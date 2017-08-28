package org.ibp.api.rest.user;

import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.brapi.v1.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Controller
public class UserResource {

	@Autowired
	private UserService userService;

	@ApiOperation(value = "List all users of one project", notes = "List all users")
	@RequestMapping(value = "/user/list", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> list(@RequestParam(value = "projectUUID") final String projectUUID) {

		final Map<String, Object> mapResults = this.userService.getUsersByProjectUUID(projectUUID);
		if (mapResults.get("ERROR") != null) {
			return new ResponseEntity<>(mapResults, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(mapResults, HttpStatus.OK);
	}
}
