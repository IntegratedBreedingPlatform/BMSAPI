package org.ibp.api.rest.program;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.dao.workbench.ProgramEligibleUsersSearchRequest;
import org.generationcp.middleware.dao.workbench.ProgramMembersSearchRequest;
import org.generationcp.middleware.domain.workbench.AddProgramMemberRequestDto;
import org.generationcp.middleware.domain.workbench.ProgramMemberDto;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.java.user.UserService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Set;

@RestController
public class ProgramMemberResource {

	@Autowired
	private ProgramService programService;

	@Autowired
	private UserService userService;

	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property(,asc|desc). " +
				"Default sort order is ascending. " +
				"Multiple sort criteria are supported.")
	})
	@ApiOperation(value = "List program members", notes = "Get the list or program members")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/members/search", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_PROGRAMS', 'MANAGE_PROGRAM_SETTINGS')")
	public ResponseEntity<List<ProgramMemberDto>> getProgramMembers(@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@ApiIgnore
		@PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable,
		@RequestBody ProgramMembersSearchRequest userSearchRequest
	) {

		return new PaginatedSearch().getPagedResult(() -> this.programService.countAllProgramMembers(programUUID, userSearchRequest),
			() -> this.programService.getProgramMembers(programUUID, userSearchRequest, pageable),
			pageable);
	}

	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property(,asc|desc). " +
				"Default sort order is ascending. " +
				"Multiple sort criteria are supported.")
	})
	@ApiOperation(value = "List users eligible to be program members", notes = "List users eligible to be program members")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/members/eligible-users/search", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_PROGRAMS', 'MANAGE_PROGRAM_SETTINGS')")
	public ResponseEntity<List<UserDto>> getMembersEligibleUsers(@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@ApiIgnore
		@PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable,
		@RequestBody ProgramEligibleUsersSearchRequest userSearchRequest
	) {

		return new PaginatedSearch().getPagedResult(() -> this.userService.countAllMembersEligibleUsers(programUUID, userSearchRequest),
			() -> this.userService.getMembersEligibleUsers(programUUID, userSearchRequest, pageable),
			pageable);
	}

	@ApiOperation(value = "Add a set of users as program members with an specific program role", notes = "Add a set of users as program members with an specific program role")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/members", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_PROGRAMS', 'MANAGE_PROGRAM_SETTINGS')")
	public ResponseEntity<Void> addProgramRoleToUsers(@PathVariable final String cropName,
		@PathVariable final String programUUID, @RequestBody final AddProgramMemberRequestDto requestDto) {
		this.programService.addNewProgramMembers(programUUID, requestDto);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Delete a set of program members from a given program", notes = "Delete a set of program members from a given program")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/members", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_PROGRAMS', 'MANAGE_PROGRAM_SETTINGS')")
	public ResponseEntity<Void> removeProgramMembers(@PathVariable final String cropName,
		@PathVariable final String programUUID, @RequestParam final Set<Integer> userIds) {
		this.programService.removeProgramMembers(programUUID, userIds);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
