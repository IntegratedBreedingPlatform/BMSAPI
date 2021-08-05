package org.ibp.api.rest.file;

import io.swagger.annotations.Api;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.generationcp.middleware.api.file.FileMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

	@RequestMapping(value = "/filemetadata", method = RequestMethod.GET)
	public ResponseEntity<List<FileMetadataDTO>> list(
		@PathVariable final String cropName,
		@RequestParam final String observationUnitUUID,
		@RequestParam(required = false) final String programUUID,
		@RequestParam(required = false) final String variableName,
		@RequestParam(required = false) final String fileName
	) {
		return new ResponseEntity<>(this.fileMetadataService.list(observationUnitUUID, programUUID, variableName, fileName), HttpStatus.OK);
	}
}
