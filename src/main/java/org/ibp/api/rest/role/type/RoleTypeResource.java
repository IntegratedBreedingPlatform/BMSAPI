package org.ibp.api.rest.role.type;

import io.swagger.annotations.ApiOperation;
import org.ibp.api.domain.role.RoleTypeDto;
import org.ibp.api.java.role.type.RoleTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Controller
public class RoleTypeResource {

	@Autowired
	private RoleTypeService roleTypeService;


		@ApiOperation(value = "gets all role types")
		@RequestMapping(value= "/role-types", method = RequestMethod.GET)
		@ResponseBody
		public ResponseEntity<List<RoleTypeDto>> getRoleTypes(){
			return new ResponseEntity<>(this.roleTypeService.getRoleTypes(), HttpStatus.OK);
		}
}
