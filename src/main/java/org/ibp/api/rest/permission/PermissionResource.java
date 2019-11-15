package org.ibp.api.rest.permission;

import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.ibp.api.java.permission.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Controller
public class PermissionResource {

	@Autowired
	private PermissionService permissionService;

	@ApiOperation(value = "Get the permissions tree for a given type", notes = "Get the permissions tree for a given type")
	@RequestMapping(value = "/permissions/tree", method = RequestMethod.GET)
	public ResponseEntity<PermissionDto> list(@RequestParam final Integer roleTypeId) {
		return new ResponseEntity<>(this.permissionService.getPermissionTree(roleTypeId), HttpStatus.OK);
	}

}
