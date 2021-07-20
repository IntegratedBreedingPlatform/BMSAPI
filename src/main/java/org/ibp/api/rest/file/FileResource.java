package org.ibp.api.rest.file;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

	/**
	 * @return Map<String, String> to overcome angularjs limitation
	 */
	@RequestMapping(value = "/files", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<String, String>> upload(
		@PathVariable final String cropName,
		@RequestPart("file") final MultipartFile file,
		@ApiParam("store file under this path") @RequestParam final String path,
		@RequestParam final String observationUnitId
	) {
		this.fileValidator.validateFile(new MapBindingResult(new HashMap<>(), String.class.getName()), file);
		this.fileStorageService.upload(file, path);
		final String fileUUID = this.fileMetadataService.save(file, path, observationUnitId);
		return new ResponseEntity<>(Collections.singletonMap("fileUUID", fileUUID), HttpStatus.CREATED);
	}

	@RequestMapping(value = "/files/**", method = RequestMethod.GET)
	@ResponseBody
	public byte[] getFile(
		@PathVariable final String cropName,
		final HttpServletRequest request
	) {
		final String path = getPath(request);
		return this.fileStorageService.getFile(path);
	}

	/**
	 * @return Map<String, String> to overcome angularjs limitation
	 */
	@ApiOperation(value = "Get predetermined file path based on parameters")
	@RequestMapping(value = "/filepath", method = RequestMethod.GET)
	public ResponseEntity<Map<String, String>> getFilePath(
		@PathVariable final String cropName,
		@RequestParam final String observationUnitId,
		@RequestParam final Integer termId,
		@RequestParam final String fileName
	) {
		final String path = this.fileMetadataService.getFilePath(observationUnitId, termId, fileName);
		return new ResponseEntity<>(Collections.singletonMap("path", path), HttpStatus.OK);
	}

	private static String getPath(final HttpServletRequest request) {
		final String path = request.getRequestURI().split(".*\\/files\\/")[1];
		return URLDecoder.decode(path);
	}

}
