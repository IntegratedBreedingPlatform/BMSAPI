package org.ibp.api.rest.file;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.ibp.api.java.file.FileMetadataService;
import org.ibp.api.java.file.FileStorageService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.file.validator.FileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Api("File services")
@RestController
@RequestMapping("/crops/{cropName}")
public class FileResource {

	private static final Logger LOG = LoggerFactory.getLogger(FileResource.class);

	/*
	 * To simplify, we put them all together in a single bag (studies and germplasm).
	 * In the frontend we can differentiate between both
	 */
	private static final String MANAGE_FILES_PERMISSIONS = "'ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM', 'EDIT_GERMPLASM', 'MG_MANAGE_FILES'"
		+ ", 'STUDIES', 'MANAGE_STUDIES', 'MS_MANAGE_OBSERVATION_UNITS', 'MS_MANAGE_FILES'";

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private FileMetadataService fileMetadataService;

	@Autowired
	private FileValidator fileValidator;

	@RequestMapping(value = "/files", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority(" + MANAGE_FILES_PERMISSIONS + ")")
	@ResponseBody
	public ResponseEntity<FileMetadataDTO> upload(
		@PathVariable final String cropName,
		@RequestPart("file") final MultipartFile file,
		@RequestParam(required = false) final String observationUnitUUID,
		@RequestParam(required = false) final String germplasmUUID,
		@RequestParam(required = false) final Integer termId
	) {
		this.fileValidator.validateFile(new MapBindingResult(new HashMap<>(), String.class.getName()), file);
		BaseValidator.checkArgument(isBlank(observationUnitUUID) != isBlank(germplasmUUID), "file.upload.entity.invalid");
		final FileMetadataDTO fileMetadataDTO = this.fileMetadataService.upload(file, observationUnitUUID, germplasmUUID, termId);
		return new ResponseEntity<>(fileMetadataDTO, HttpStatus.CREATED);
	}

	/*
	 * NOTE: with GET /files/{fileUUID} we get some url sanitization issues in the frontend:
	 * WARNING: sanitizing unsafe URL value
	 */
	@RequestMapping(value = "/files/**", method = RequestMethod.GET)
	@ResponseBody
	public byte[] getFile(
		@PathVariable final String cropName,
		final HttpServletRequest request
	) {
		final String path = getPath(request);
		return this.fileStorageService.getFile(path);
	}

	private static String getPath(final HttpServletRequest request) {
		final String path = request.getRequestURI().split(".*\\/files\\/")[1];
		return URLDecoder.decode(path);
	}

	@RequestMapping(value = "/files/{fileUUID}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyAuthority(" + MANAGE_FILES_PERMISSIONS + ")")
	@ResponseBody
	public ResponseEntity<Void> deleteFile(
		@PathVariable final String cropName,
		@PathVariable final String fileUUID
	) {
		this.fileMetadataService.delete(fileUUID);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * @return Map<String, String> to overcome angularjs limitation
	 */
	@ApiOperation("Get file storage status: true => active")
	@RequestMapping(value = "/filestorage/status", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Boolean>> getFileStorageStatus(
		@PathVariable final String cropName
	) {
		return new ResponseEntity<>(Collections.singletonMap("status", this.fileStorageService.isConfigured()), HttpStatus.OK);
	}

}
