package org.ibp.api.rest.file;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.generationcp.middleware.api.file.FileMetadataFilterRequest;
import org.generationcp.middleware.api.file.FileMetadataService;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.file.FileStorageService;
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

import javax.servlet.http.HttpServletRequest;
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

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private HttpServletRequest request;

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
		@RequestParam(required = false) final String germplasmUUID,
		@RequestParam(required = false) final Integer instanceId
	) {
		final Integer count = this.fileMetadataServiceMiddleware.getAll(variableIds, datasetId, germplasmUUID, instanceId).size();
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
		@RequestParam(required = false) final String germplasmUUID,
		@RequestParam(required = false) final Integer instanceId
	) {
		//Check if only one of the parameters has value
		final boolean valid = ((datasetId == null? 0 : 1) + (isBlank(germplasmUUID)? 0 : 1) + ((instanceId == null)? 0 : 1)) == 1;
		BaseValidator.checkArgument(valid, "file.upload.detach.parameters.invalid");
		if (datasetId != null || instanceId != null) {
			FileResource.verifyHasAuthorityStudy(this.request);
		} else {
			FileResource.verifyHasAuthorityGermplasm(this.request);
		}

		this.fileMetadataServiceMiddleware.detachFiles(variableIds, datasetId, germplasmUUID, instanceId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/filemetadata", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeFiles(
		@PathVariable final String cropName,
		@RequestParam final List<Integer> variableIds,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final Integer datasetId,
		@RequestParam(required = false) final String germplasmUUID,
		@RequestParam(required = false) final Integer instanceId
	) {
		//Check if only one of the parameters has value
		final boolean valid = ((datasetId == null? 0 : 1) + (isBlank(germplasmUUID)? 0 : 1) + ((instanceId == null)? 0 : 1)) == 1;
		BaseValidator.checkArgument(valid, "file.upload.detach.parameters.invalid");
		this.validateFileStorage();
		if (datasetId != null || instanceId != null) {
			FileResource.verifyHasAuthorityStudy(this.request);
		} else {
			FileResource.verifyHasAuthorityGermplasm(this.request);
		}

		this.fileMetadataService.removeFiles(variableIds, datasetId, germplasmUUID, instanceId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private void validateFileStorage() {
		BaseValidator.checkArgument(this.fileStorageService.isConfigured(), "file.storage.not.configured");
	}
}
