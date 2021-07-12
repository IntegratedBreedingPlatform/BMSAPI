package org.ibp.api.rest.program;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.workbench.ProgramMemberDto;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.program.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
public class ProgramMemberResource {

	@Autowired
	private ProgramService programService;

	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.")
	})
	@ApiOperation(value = "List program members", notes = "Get the list or program members")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/members", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public ResponseEntity<ProgramMemberDto> getProgramMembers(@PathVariable final String cropName, @PathVariable final String programUUID,
		@ApiIgnore
		@PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable) {
		return null;
	}

}
