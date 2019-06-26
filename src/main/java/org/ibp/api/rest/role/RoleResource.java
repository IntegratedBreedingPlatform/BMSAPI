package org.ibp.api.rest.role;

import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.ibp.api.domain.role.RoleDto;
import org.ibp.api.java.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Controller
public class RoleResource {

	@Autowired
	private RoleService roleService;

	@ApiOperation(value = "Filter roles", notes = "Filter roles")
	@RequestMapping(value = "/roles/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<RoleDto>> getFilteredRoles(@RequestBody final RoleSearchDto searchDTO) {
		return new ResponseEntity<>(this.roleService.getRoles(searchDTO), HttpStatus.OK);
	}

}
