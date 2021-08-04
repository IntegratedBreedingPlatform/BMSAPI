package org.ibp.api.rest.file;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.ibp.api.java.file.FileMetadataService;
import org.ibp.api.java.file.FileStorageService;
import org.ibp.api.java.impl.middleware.file.validator.FileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@Api("File services")
@RestController
@RequestMapping("/crops/{cropName}")
public class FileResource {

	private static final Logger LOG = LoggerFactory.getLogger(FileResource.class);

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private FileMetadataService fileMetadataService;

	@Autowired
	private FileValidator fileValidator;

	@RequestMapping(value = "/files", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<FileMetadataDTO> upload(
		@PathVariable final String cropName,
		@RequestPart("file") final MultipartFile file,
		@RequestParam final String observationUnitUUID,
		@RequestParam(required = false) final Integer termId
	) {
		this.fileValidator.validateFile(new MapBindingResult(new HashMap<>(), String.class.getName()), file);
		final FileMetadataDTO fileMetadataDTO = this.fileMetadataService.upload(file, observationUnitUUID, termId);
		return new ResponseEntity<>(fileMetadataDTO, HttpStatus.CREATED);
	}

	// TODO now that we have file_metadata, we can GET /files/{fileUUID} instead of using path
	@RequestMapping(value = "/files/**", method = RequestMethod.GET)
	@ResponseBody
	public byte[] getFile(
		@PathVariable final String cropName,
		final HttpServletRequest request
	) {
		final String path = getPath(request);
		return this.fileStorageService.getFile(path);
	}

	@RequestMapping(value = "/files/{fileUUID}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Void> deleteFile(
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
	public ResponseEntity<Map<String, Boolean>> getFileStorageStatus() {
		return new ResponseEntity<>(Collections.singletonMap("status", this.fileStorageService.isConfigured()), HttpStatus.OK);
	}

	private static String getPath(final HttpServletRequest request) {
		final String path = request.getRequestURI().split(".*\\/files\\/")[1];
		return URLDecoder.decode(path);
	}

}
