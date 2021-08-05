package org.ibp.api.rest.file;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.generationcp.middleware.api.file.FileMetadataFilterRequest;
import org.generationcp.middleware.api.file.FileMetadataService;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.rest.common.PaginatedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * Separated from FileResource mostly because of wildcard get (files/**)
 */
@Api("FileMetadata services")
@RequestMapping("/crops/{cropName}")
@RestController
public class FileMetadataResource {

	@Autowired
	private FileMetadataService fileMetadataService;

	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.")
	})
	@RequestMapping(value = "/filemetadata/search", method = RequestMethod.POST)
	public ResponseEntity<List<FileMetadataDTO>> search(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final FileMetadataFilterRequest filterRequest,
		@ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable
	) {
		return new PaginatedSearch().getPagedResult(
			() -> this.fileMetadataService.countSearch(filterRequest, programUUID, pageable),
			() -> this.fileMetadataService.search(filterRequest, programUUID, pageable),
			pageable
		);
	}
}
