package org.ibp.api.rest.file;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.generationcp.middleware.api.file.FileMetadataFilterRequest;
import org.generationcp.middleware.api.file.FileMetadataService;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.rest.common.PaginatedSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Separated from FileResource mostly because of wildcard get (files/**)
 */
@Api("FileMetadata services")
@RequestMapping("/crops/{cropName}")
@RestController
public class FileMetadataResource {

	@Autowired
	private FileMetadataService fileMetadataServiceMiddleware;

	@Autowired
	private org.ibp.api.java.file.FileMetadataService fileMetadataService;

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
			() -> this.fileMetadataServiceMiddleware.countSearch(filterRequest, programUUID),
			() -> this.fileMetadataServiceMiddleware.search(filterRequest, programUUID, pageable),
			pageable
		);
	}

	@RequestMapping(value = "/filemetadata", method = RequestMethod.HEAD)
	public ResponseEntity<Void> getFileCount(
		@PathVariable final String cropName,
		@RequestParam final List<Integer> variableIds,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final Integer datasetId,
		@RequestParam(required = false) final String germplasmUUID
	) {
		final Integer count = this.fileMetadataServiceMiddleware.getAll(variableIds, datasetId, germplasmUUID).size();
		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", String.valueOf(count));
		return new ResponseEntity<>(headers, HttpStatus.OK);
	}

	@RequestMapping(value = "/filemetadata/variables", method = RequestMethod.DELETE)
	public ResponseEntity<Void> detachFiles(
		@PathVariable final String cropName,
		@RequestParam final List<Integer> variableIds,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final Integer datasetId,
		@RequestParam(required = false) final String germplasmUUID
	) {
		BaseValidator.checkArgument((datasetId == null) != isBlank(germplasmUUID), "file.upload.detach.parameters.invalid");

		this.fileMetadataServiceMiddleware.detachFiles(variableIds, datasetId, germplasmUUID);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/filemetadata", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeFiles(
		@PathVariable final String cropName,
		@RequestParam final List<Integer> variableIds,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final Integer datasetId,
		@RequestParam(required = false) final String germplasmUUID
	) {
		BaseValidator.checkArgument((datasetId == null) != isBlank(germplasmUUID), "file.upload.detach.parameters.invalid");

		this.fileMetadataService.removeFiles(variableIds, datasetId, germplasmUUID);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
