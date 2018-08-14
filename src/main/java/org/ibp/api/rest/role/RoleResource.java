
package org.ibp.api.rest.role;

import java.util.List;

import org.ibp.api.java.role.RoleService;
import org.ibp.api.domain.role.RoleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Controller
public class RoleResource {

	@Autowired
	private RoleService roleService;

	@ApiOperation(value = "List all roles", notes = "List all roles in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/roles", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<RoleDto>> listRoles() {
		return new ResponseEntity<>(this.roleService.getAllRoles(), HttpStatus.OK);
	}
	
	@ApiOperation(value = "List all assignable roles", notes = "List all assignable roles in this deployment instance of BMSAPI. ")
	@RequestMapping(value = "/roles/assignable", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<RoleDto>> listAssignableRoles() {
		return new ResponseEntity<>(this.roleService.getAssignableRoles(), HttpStatus.OK);
	}

}
