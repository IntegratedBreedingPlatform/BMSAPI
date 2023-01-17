package org.ibp.api.rest.role;

import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.service.api.user.RoleDto;
import org.generationcp.middleware.service.api.user.RoleGeneratorInput;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.ibp.api.java.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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

	@ApiOperation(value = "Save role", notes = "Save role. ")
	@RequestMapping(value = "/roles", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN')")
	@ResponseBody
	public ResponseEntity<Integer> createRole(@RequestBody final RoleGeneratorInput dto) {
		final Integer roleId = this.roleService.createRole(dto);
		return new ResponseEntity<>(roleId, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Get a role", notes = "Get role")
	@RequestMapping(value = "/roles/{id}", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN')")
	@ResponseBody
	public ResponseEntity<RoleDto> getRole(final @PathVariable Integer id) {
		return new ResponseEntity<>(roleService.getRole(id), HttpStatus.OK);
	}

	@ApiOperation(value = "Update role", notes = "Update role. ")
	@RequestMapping(value = "/roles", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyAuthority('ADMIN','ADMINISTRATION','SITE_ADMIN')")
	@ResponseBody
	public ResponseEntity updateRole(@RequestBody final RoleGeneratorInput roleGeneratorInput) {
		this.roleService.updateRole(roleGeneratorInput);
		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}

}
