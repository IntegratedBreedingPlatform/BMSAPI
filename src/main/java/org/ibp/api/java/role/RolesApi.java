package org.ibp.api.java.role;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/roles")
public class RolesApi {

	@Autowired
	private RoleService roleService;

	@ApiOperation(value = "List all roles", notes = "List all roles in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/listRoles", method = RequestMethod.GET)
	public ResponseEntity<List<RoleData>> listAvailableusers() {
		return new ResponseEntity<List<RoleData>>(this.roleService.getAllRoles(), HttpStatus.OK);
	}

}
